/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2013 Jean-Noël Rouvignac - initial API and implementation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program under LICENSE-GNUGPL.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution under LICENSE-ECLIPSE, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.autorefactor.samples_out;

public class BooleanSample {

    private boolean f;
    private Boolean g;

    public void useBooleanConstants() {
        Boolean b1 = Boolean.TRUE;
        Boolean b2 = Boolean.FALSE;
    }

    // TODO JNR handle mix and match of boolean primitives and boolean objects

    public boolean returnIfConditionBooleanPrimitive(boolean b) {
        return b;
    }

    public boolean returnIfConditionBooleanPrimitive2(boolean b) {
        return !b;
    }

    public Boolean returnIfConditionBooleanObject(boolean b) {
        return Boolean.valueOf(b);
    }

    public Boolean returnIfConditionBooleanObject2(boolean b) {
        return Boolean.valueOf(!b);
    }

    public boolean returnIfConditionMixedBoolean1(boolean b) {
        return b;
    }

    public boolean returnIfConditionMixedBoolean2(boolean b) {
        return b;
    }

    public boolean returnIfConditionBooleanPrimitive3(boolean b) {
        return b;
    }

    public boolean returnIfConditionBooleanPrimitive4(boolean b) {
        return !b;
    }

    public boolean returnIfConditionBooleanObject3(boolean b) {
        return b;
    }

    public boolean returnIfConditionBooleanObject4(boolean b) {
        return !b;
    }

    public void removeUselessUseOfTernaryOperatorWithBooleanPrimitive1(
            boolean bo) {
        boolean b = bo;
    }

    public void removeUselessUseOfTernaryOperatorWithBooleanPrimitive2(
            boolean bo) {
        boolean b = !bo;
    }

    public void removeUselessUseOfTernaryOperatorWithBooleanObject1(boolean bo) {
        Boolean b = Boolean.valueOf(bo);
    }

    public void removeUselessUseOfTernaryOperatorWithBooleanObject2(boolean bo) {
        Boolean b = Boolean.valueOf(!bo);
    }

    public void removeUselessIfInBooleanPrimitiveAssignment1(boolean bo) {
        boolean b = !bo;
    }

    public void removeUselessIfInBooleanPrimitiveAssignment2(boolean bo) {
        boolean b = bo;
    }

    public void removeUselessIfInBooleanObjectAssignment1(boolean bo) {
        Boolean b = Boolean.valueOf(!bo);
    }

    public void removeUselessIfInBooleanObjectAssignment2(boolean bo) {
        boolean b = bo;
    }

    public void removeUselessIfInBooleanPrimitiveAssignment3(boolean bo,
            boolean b) {
        b = !bo;
    }

    public void removeUselessIfInBooleanPrimitiveAssignment4(boolean bo,
            boolean b) {
        b = bo;
    }

    public void removeUselessIfInBooleanPrimitiveAssignmentSearchFurtherAwayForPreviousSibling(
            boolean bo, boolean b) {
        b = false;
        char c = 'a';
        byte by = 0;
        double d = 0.0;
        if (bo) {
            b = true;
        }
    }

    public void removeUselessIfInBooleanObjectAssignment3(boolean bo, Boolean b) {
        b = Boolean.valueOf(!bo);
    }

    public void removeUselessIfInBooleanObjectAssignment4(boolean bo, Boolean b) {
        b = Boolean.valueOf(bo);
    }

    public void removeUselessIfInBooleanPrimitiveAssignment5(boolean bo) {
        this.f = bo;
    }

    public void removeUselessIfInBooleanObjectAssignment5(boolean bo) {
        this.g = Boolean.valueOf(bo);
    }

    public void removeUselessIfInBooleanPrimitiveAssignment6(boolean bo) {
        f = bo;
    }

    public void removeUselessIfInBooleanObjectAssignment6(boolean bo) {
        g = Boolean.valueOf(bo);
    }

    public void removeUselessIfInBooleanObjectAssignment7(boolean bo) {
        BooleanSample.this.g = Boolean.valueOf(bo);
    }

    // TODO redo the next 4 with Boolean object

    public boolean removeUselessIfInBooleanPrimitiveAssignment7(boolean bo) {
        return bo && aMethodThatReturnsBoolean();
    }

    public boolean removeUselessIfInBooleanPrimitiveAssignment8(boolean bo) {
        return !bo || aMethodThatReturnsBoolean();
    }

    public boolean removeUselessIfInBooleanPrimitiveAssignment9(boolean bo) {
        return !bo && aMethodThatReturnsBoolean();
    }

    public boolean removeUselessIfInBooleanPrimitiveAssignment10(boolean bo) {
        return bo || aMethodThatReturnsBoolean();
    }

    // TODO aMethodThatAcceptsABoolean(bo ? true : false);
    // TODO aMethodThatAcceptsABoolean(bo ? false : true);
    // TODO aMethodThatAcceptsABoolean(bo ? Boolean.TRUE : Boolean.FALSE);
    // TODO aMethodThatAcceptsABoolean(bo ? Boolean.FALSE : Boolean.TRUE);

    public void removeUselessIfInBooleanPrimitiveExpression10(boolean bo) {
        aMethodThatAcceptsABoolean(bo);
    }

    public void removeUselessIfInBooleanPrimitiveExpression11(boolean bo) {
        aMethodThatAcceptsABoolean(!bo);
    }

    private boolean aMethodThatReturnsBoolean() {
        return false;
    }

    private void aMethodThatAcceptsABoolean(boolean b) {
    }

}
