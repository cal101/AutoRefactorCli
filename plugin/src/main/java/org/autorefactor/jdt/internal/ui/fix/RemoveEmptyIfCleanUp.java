/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2013-2017 Jean-Noël Rouvignac - initial API and implementation
 * Copyright (C) 2017 Fabrice Tiercelin - Inline the blocks
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
package org.autorefactor.jdt.internal.ui.fix;

import org.autorefactor.jdt.core.dom.ASTRewrite;
import org.autorefactor.jdt.internal.corext.dom.ASTNodeFactory;
import org.autorefactor.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;

/** See {@link #getDescription()} method. */
public class RemoveEmptyIfCleanUp extends AbstractCleanUpRule {
    /**
     * Get the name.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return MultiFixMessages.CleanUpRefactoringWizard_RemoveEmptyIfCleanUp_name;
    }

    /**
     * Get the description.
     *
     * @return the description.
     */
    @Override
    public String getDescription() {
        return MultiFixMessages.CleanUpRefactoringWizard_RemoveEmptyIfCleanUp_description;
    }

    /**
     * Get the reason.
     *
     * @return the reason.
     */
    @Override
    public String getReason() {
        return MultiFixMessages.CleanUpRefactoringWizard_RemoveEmptyIfCleanUp_reason;
    }

    @Override
    public boolean visit(final IfStatement node) {
        ASTRewrite rewrite= cuRewrite.getASTRewrite();

        Statement thenStatement= node.getThenStatement();
        Statement elseStatement= node.getElseStatement();
        if (elseStatement != null && ASTNodes.asList(elseStatement).isEmpty()) {
            rewrite.remove(elseStatement);
            return false;
        }
        if (thenStatement != null && ASTNodes.asList(thenStatement).isEmpty()) {
            ASTNodeFactory ast= cuRewrite.getASTBuilder();

            Expression condition= node.getExpression();
            if (elseStatement != null) {
                rewrite.replace(node, ast.if0(ast.negate(condition), rewrite.createMoveTarget(elseStatement)));
            } else if (ASTNodes.isPassiveWithoutFallingThrough(condition)) {
                removeBlock(node, rewrite, ast);
                return false;
            }
        }

        return true;
    }

    private void removeBlock(final IfStatement node, final ASTRewrite rewrite, final ASTNodeFactory ast) {
        if (ASTNodes.canHaveSiblings(node)) {
            rewrite.remove(node);
        } else {
            rewrite.replace(node, ast.block());
        }
    }
}
