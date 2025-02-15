package cgeo.geocaching.brouter.expressions;

import androidx.annotation.NonNull;

import java.util.StringTokenizer;

final class BExpression {
    private static final int OR_EXP = 10;
    private static final int AND_EXP = 11;
    private static final int NOT_EXP = 12;

    private static final int ADD_EXP = 20;
    private static final int MULTIPLY_EXP = 21;
    private static final int DIVIDE_EXP = 22;
    private static final int MAX_EXP = 23;
    private static final int EQUAL_EXP = 24;
    private static final int GREATER_EXP = 25;
    private static final int MIN_EXP = 26;

    private static final int SUB_EXP = 27;
    private static final int LESSER_EXP = 28;
    private static final int XOR_EXP = 29;

    private static final int SWITCH_EXP = 30;
    private static final int ASSIGN_EXP = 31;
    private static final int LOOKUP_EXP = 32;
    private static final int NUMBER_EXP = 33;
    private static final int VARIABLE_EXP = 34;
    private static final int FOREIGN_VARIABLE_EXP = 35;
    private static final int VARIABLE_GET_EXP = 36;

    private int typ;
    private BExpression op1;
    private BExpression op2;
    private BExpression op3;
    private float numberValue;
    private int variableIdx;
    private int lookupNameIdx = -1;
    private int[] lookupValueIdxArray;
    private boolean doNotChange;

    // Parse the expression and all subexpression
    public static BExpression parse(final BExpressionContext ctx, final int level) throws Exception {
        return parse(ctx, level, null);
    }

    private static BExpression parse(final BExpressionContext ctx, final int level, final String optionalToken) throws Exception {
        BExpression e = parseRaw(ctx, level, optionalToken);
        if (e == null) {
            return null;
        }

        if (ASSIGN_EXP == e.typ) {
            // manage assined an injected values
            final BExpression assignedBefore = ctx.lastAssignedExpression.get(e.variableIdx);
            if (assignedBefore != null && assignedBefore.doNotChange) {
                e.op1 = assignedBefore; // was injected as key-value
                e.op1.doNotChange = false; // protect just once, can be changed in second assignement
            }
            ctx.lastAssignedExpression.set(e.variableIdx, e.op1);
        } else if (!ctx.skipConstantExpressionOptimizations) {
            // try to simplify the expression
            if (VARIABLE_EXP == e.typ) {
                final BExpression ae = ctx.lastAssignedExpression.get(e.variableIdx);
                if (ae != null && ae.typ == NUMBER_EXP) {
                    e = ae;
                }
            } else {
                final BExpression eCollapsed = e.tryCollapse();
                if (e != eCollapsed) {
                    e = eCollapsed; // allow breakpoint..
                }
                final BExpression eEvaluated = e.tryEvaluateConstant();
                if (e != eEvaluated) {
                    e = eEvaluated; // allow breakpoint..
                }
            }
        }
        if (level == 0) {
            // mark the used lookups after the
            // expression is collapsed to not mark
            // lookups as used that appear in the profile
            // but are de-activated by constant expressions
            final int nodeCount = e.markLookupIdxUsed(ctx);
            ctx.expressionNodeCount += nodeCount;
        }
        return e;
    }

    private int markLookupIdxUsed(BExpressionContext ctx) {
        int nodeCount = 1;
        if (lookupNameIdx >= 0) {
            ctx.markLookupIdxUsed(lookupNameIdx);
        }
        if (op1 != null) {
            nodeCount += op1.markLookupIdxUsed(ctx);
        }
        if (op2 != null) {
            nodeCount += op2.markLookupIdxUsed(ctx);
        }
        if (op3 != null) {
            nodeCount += op3.markLookupIdxUsed(ctx);
        }
        return nodeCount;
    }

    @SuppressWarnings("PMD.NPathComplexity")
    private static BExpression parseRaw(BExpressionContext ctx, int level, String optionalToken) throws Exception {
        boolean brackets = false;
        String operator = ctx.parseToken();
        if (optionalToken != null && optionalToken.equals(operator)) {
            operator = ctx.parseToken();
        }
        if ("(".equals(operator)) {
            brackets = true;
            operator = ctx.parseToken();
        }

        if (operator == null) {
            if (level == 0) {
                return null;
            } else {
                throw new IllegalArgumentException("unexpected end of file");
            }
        }

        if (level == 0 && !"assign".equals(operator)) {
            throw new IllegalArgumentException("operator " + operator + " is invalid on toplevel (only 'assign' allowed)");
        }

        final BExpression exp = new BExpression();
        int nops = 3;
        boolean ifThenElse = false;

        if ("switch".equals(operator)) {
            exp.typ = SWITCH_EXP;
        } else if ("if".equals(operator)) {
            exp.typ = SWITCH_EXP;
            ifThenElse = true;
        } else {
            nops = 2; // check binary expressions

            if ("or".equals(operator)) {
                exp.typ = OR_EXP;
            } else if ("and".equals(operator)) {
                exp.typ = AND_EXP;
            } else if ("multiply".equals(operator)) {
                exp.typ = MULTIPLY_EXP;
            } else if ("divide".equals(operator)) {
                exp.typ = DIVIDE_EXP;
            } else if ("add".equals(operator)) {
                exp.typ = ADD_EXP;
            } else if ("max".equals(operator)) {
                exp.typ = MAX_EXP;
            } else if ("min".equals(operator)) {
                exp.typ = MIN_EXP;
            } else if ("equal".equals(operator)) {
                exp.typ = EQUAL_EXP;
            } else if ("greater".equals(operator)) {
                exp.typ = GREATER_EXP;
            } else if ("sub".equals(operator)) {
                exp.typ = SUB_EXP;
            } else if ("lesser".equals(operator)) {
                exp.typ = LESSER_EXP;
            } else if ("xor".equals(operator)) {
                exp.typ = XOR_EXP;
            } else {
                nops = 1; // check unary expressions
                if ("assign".equals(operator)) {
                    if (level > 0) {
                        throw new IllegalArgumentException("assign operator within expression");
                    }
                    exp.typ = ASSIGN_EXP;
                    final String variable = ctx.parseToken();
                    if (variable == null) {
                        throw new IllegalArgumentException("unexpected end of file");
                    }
                    if (variable.indexOf('=') >= 0) {
                        throw new IllegalArgumentException("variable name cannot contain '=': " + variable);
                    }
                    if (variable.indexOf(':') >= 0) {
                        throw new IllegalArgumentException("cannot assign context-prefixed variable: " + variable);
                    }
                    exp.variableIdx = ctx.getVariableIdx(variable, true);
                    if (exp.variableIdx < ctx.getMinWriteIdx()) {
                        throw new IllegalArgumentException("cannot assign to readonly variable " + variable);
                    }
                } else if ("not".equals(operator)) {
                    exp.typ = NOT_EXP;
                } else {
                    nops = 0; // check elemantary expressions
                    int idx = operator.indexOf('=');
                    if (idx >= 0) {
                        exp.typ = LOOKUP_EXP;
                        final String name = operator.substring(0, idx);
                        final String values = operator.substring(idx + 1);

                        exp.lookupNameIdx = ctx.getLookupNameIdx(name);
                        if (exp.lookupNameIdx < 0) {
                            throw new IllegalArgumentException("unknown lookup name: " + name);
                        }
                        final StringTokenizer tk = new StringTokenizer(values, "|");
                        final int nt = tk.countTokens();
                        final int nt2 = nt == 0 ? 1 : nt;
                        exp.lookupValueIdxArray = new int[nt2];
                        for (int ti = 0; ti < nt2; ti++) {
                            final String value = ti < nt ? tk.nextToken() : "";
                            exp.lookupValueIdxArray[ti] = ctx.getLookupValueIdx(exp.lookupNameIdx, value);
                            if (exp.lookupValueIdxArray[ti] < 0) {
                                throw new IllegalArgumentException("unknown lookup value: " + value);
                            }
                        }
                    } else {
                        idx = operator.indexOf(':');
                        if (idx >= 0) {
                            /*
                            use of variable values
                            assign no_height
                                switch and not      maxheight=
                                           lesser v:maxheight  my_height  true
                                false
                            */
                            if (operator.startsWith("v:")) {
                                final String name = operator.substring(2);
                                exp.typ = VARIABLE_GET_EXP;
                                exp.lookupNameIdx = ctx.getLookupNameIdx(name);
                            } else {
                                final String context = operator.substring(0, idx);
                                final String varname = operator.substring(idx + 1);
                                exp.typ = FOREIGN_VARIABLE_EXP;
                                exp.variableIdx = ctx.getForeignVariableIdx(context, varname);
                            }
                        } else {
                            idx = ctx.getVariableIdx(operator, false);
                            if (idx >= 0) {
                                exp.typ = VARIABLE_EXP;
                                exp.variableIdx = idx;
                            } else if ("true".equals(operator)) {
                                exp.numberValue = 1.f;
                                exp.typ = NUMBER_EXP;
                            } else if ("false".equals(operator)) {
                                exp.numberValue = 0.f;
                                exp.typ = NUMBER_EXP;
                            } else {
                                try {
                                    exp.numberValue = Float.parseFloat(operator);
                                    exp.typ = NUMBER_EXP;
                                } catch (NumberFormatException nfe) {
                                    throw new IllegalArgumentException("unknown expression: " + operator);
                                }
                            }
                        }
                    }
                }
            }
        }
        // parse operands
        if (nops > 0) {
            exp.op1 = parse(ctx, level + 1, exp.typ == ASSIGN_EXP ? "=" : null);
        }
        if (nops > 1) {
            if (ifThenElse) {
                checkExpectedToken(ctx, "then");
            }
            exp.op2 = parse(ctx, level + 1, null);
        }
        if (nops > 2) {
            if (ifThenElse) {
                checkExpectedToken(ctx, "else");
            }
            exp.op3 = parse(ctx, level + 1, null);
        }
        if (brackets) {
            checkExpectedToken(ctx, ")");
        }
        return exp;
    }

    private static void checkExpectedToken(final BExpressionContext ctx, final String expected) throws Exception {
        final String token = ctx.parseToken();
        if (!expected.equals(token)) {
            throw new IllegalArgumentException("unexpected token: " + token + ", expected: " + expected);
        }
    }

    // Evaluate the expression
    public float evaluate(final BExpressionContext ctx) {
        switch (typ) {
            case OR_EXP:
                return op1.evaluate(ctx) != 0.f ? 1.f : (op2.evaluate(ctx) != 0.f ? 1.f : 0.f);
            case XOR_EXP:
                return (op1.evaluate(ctx) != 0.f) ^ (op2.evaluate(ctx) != 0.f) ? 1.f : 0.f;
            case AND_EXP:
                return op1.evaluate(ctx) != 0.f ? (op2.evaluate(ctx) != 0.f ? 1.f : 0.f) : 0.f;
            case ADD_EXP:
                return op1.evaluate(ctx) + op2.evaluate(ctx);
            case SUB_EXP:
                return op1.evaluate(ctx) - op2.evaluate(ctx);
            case MULTIPLY_EXP:
                return op1.evaluate(ctx) * op2.evaluate(ctx);
            case DIVIDE_EXP:
                return divide(op1.evaluate(ctx), op2.evaluate(ctx));
            case MAX_EXP:
                return max(op1.evaluate(ctx), op2.evaluate(ctx));
            case MIN_EXP:
                return min(op1.evaluate(ctx), op2.evaluate(ctx));
            case EQUAL_EXP:
                return op1.evaluate(ctx) == op2.evaluate(ctx) ? 1.f : 0.f;
            case GREATER_EXP:
                return op1.evaluate(ctx) > op2.evaluate(ctx) ? 1.f : 0.f;
            case LESSER_EXP:
                return op1.evaluate(ctx) < op2.evaluate(ctx) ? 1.f : 0.f;
            case SWITCH_EXP:
                return op1.evaluate(ctx) != 0.f ? op2.evaluate(ctx) : op3.evaluate(ctx);
            case ASSIGN_EXP:
                return ctx.assign(variableIdx, op1.evaluate(ctx));
            case LOOKUP_EXP:
                return ctx.getLookupMatch(lookupNameIdx, lookupValueIdxArray);
            case NUMBER_EXP:
                return numberValue;
            case VARIABLE_EXP:
                return ctx.getVariableValue(variableIdx);
            case FOREIGN_VARIABLE_EXP:
                return ctx.getForeignVariableValue(variableIdx);
            case VARIABLE_GET_EXP:
                return ctx.getLookupValue(lookupNameIdx);
            case NOT_EXP:
                return op1.evaluate(ctx) == 0.f ? 1.f : 0.f;
            default:
                throw new IllegalArgumentException("unknown op-code: " + typ);
        }
    }

    // Try to collapse the expression
    // if logically possible
    private BExpression tryCollapse() {
        switch (typ) {
            case OR_EXP:
                return NUMBER_EXP == op1.typ ?
                        (op1.numberValue != 0.f ? op1 : op2)
                        : (NUMBER_EXP == op2.typ ?
                        (op2.numberValue != 0.f ? op2 : op1)
                        : this);
            case AND_EXP:
                return NUMBER_EXP == op1.typ ?
                        (op1.numberValue == 0.f ? op1 : op2)
                        : (NUMBER_EXP == op2.typ ?
                        (op2.numberValue == 0.f ? op2 : op1)
                        : this);
            case ADD_EXP:
                return NUMBER_EXP == op1.typ ?
                        (op1.numberValue == 0.f ? op2 : this)
                        : (NUMBER_EXP == op2.typ ?
                        (op2.numberValue == 0.f ? op1 : this)
                        : this);
            case SWITCH_EXP:
                return NUMBER_EXP == op1.typ ?
                        (op1.numberValue == 0.f ? op3 : op2) : this;
            default:
                return this;
        }
    }

    // Try to evaluate the expression
    // if all operands are constant
    private BExpression tryEvaluateConstant() {
        if (op1 != null && NUMBER_EXP == op1.typ
                && (op2 == null || NUMBER_EXP == op2.typ)
                && (op3 == null || NUMBER_EXP == op3.typ)) {
            final BExpression exp = new BExpression();
            exp.typ = NUMBER_EXP;
            exp.numberValue = evaluate(null);
            return exp;
        }
        return this;
    }

    private float max(final float v1, final float v2) {
        return Math.max(v1, v2);
    }

    private float min(final float v1, final float v2) {
        return Math.min(v1, v2);
    }

    private float divide(float v1, float v2) {
        if (v2 == 0f) {
            throw new IllegalArgumentException("div by zero");
        }
        return v1 / v2;
    }

    @NonNull
    @Override
    public String toString() {
        if (typ == NUMBER_EXP) {
            return "" + numberValue;
        }
        if (typ == VARIABLE_EXP) {
            return "vidx=" + variableIdx;
        }
        final StringBuilder sb = new StringBuilder("typ=" + typ + " ops=(");
        addOp(sb, op1);
        addOp(sb, op2);
        addOp(sb, op3);
        sb.append(')');
        return sb.toString();
    }

    private void addOp(StringBuilder sb, BExpression e) {
        if (e != null) {
            sb.append('[').append(e).append(']');
        }
    }

    static BExpression createAssignExpressionFromKeyValue(BExpressionContext ctx, String key, String value) {
        final BExpression e = new BExpression();
        e.typ = ASSIGN_EXP;
        e.variableIdx = ctx.getVariableIdx(key, true);
        e.op1 = new BExpression();
        e.op1.typ = NUMBER_EXP;
        e.op1.numberValue = Float.parseFloat(value);
        e.op1.doNotChange = true;
        ctx.lastAssignedExpression.set(e.variableIdx, e.op1);
        return e;
    }
}
