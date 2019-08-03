/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2013-2018 Jean-Noël Rouvignac - initial API and implementation
 * Copyright (C) 2018 Andrei Paikin - Remove protected modifier for final class not inherited members.
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

import static org.autorefactor.jdt.internal.corext.dom.ASTNodes.hasType;
import static org.autorefactor.jdt.internal.corext.dom.ASTNodes.modifiers;
import static org.autorefactor.jdt.internal.corext.dom.ASTNodes.resources;
import static org.eclipse.jdt.core.dom.Modifier.isFinal;
import static org.eclipse.jdt.core.dom.Modifier.isPrivate;
import static org.eclipse.jdt.core.dom.Modifier.isProtected;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.autorefactor.jdt.internal.corext.dom.ASTBuilder;
import org.autorefactor.util.NotImplementedException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

/** See {@link #getDescription()} method. */
public class RedundantModifiersCleanUp extends AbstractCleanUpRule {
    /**
     * Get the name.
     *
     * @return the name.
     */
    public String getName() {
        return MultiFixMessages.CleanUpRefactoringWizard_RedundantModifiersCleanUp_name;
    }

    /**
     * Get the description.
     *
     * @return the description.
     */
    public String getDescription() {
        return MultiFixMessages.CleanUpRefactoringWizard_RedundantModifiersCleanUp_description;
    }

    /**
     * Get the reason.
     *
     * @return the reason.
     */
    public String getReason() {
        return MultiFixMessages.CleanUpRefactoringWizard_RedundantModifiersCleanUp_reason;
    }

    private static final class ModifierOrderComparator implements Comparator<IExtendedModifier> {
        /**
         * Compare objects.
         *
         * @param o1 First item
         * @param o2 Second item
         *
         * @return -1, 0 or 1
         */
        public int compare(IExtendedModifier o1, IExtendedModifier o2) {
            if (o1.isAnnotation()) {
                if (o2.isAnnotation()) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (o2.isAnnotation()) {
                return 1;
            } else {
                final int i1= ORDERED_MODIFIERS.indexOf(((Modifier) o1).getKeyword());
                final int i2= ORDERED_MODIFIERS.indexOf(((Modifier) o2).getKeyword());
                if (i1 == -1) {
                    throw new NotImplementedException(((Modifier) o1), "cannot determine order for modifier " + o1);
                }
                if (i2 == -1) {
                    throw new NotImplementedException(((Modifier) o2), "cannot compare modifier " + o2);
                }
                return i1 - i2;
            }
        }
    }

    private static final List<ModifierKeyword> ORDERED_MODIFIERS= Collections.unmodifiableList(Arrays.asList(
            ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD,
            ModifierKeyword.ABSTRACT_KEYWORD, ModifierKeyword.STATIC_KEYWORD, ModifierKeyword.FINAL_KEYWORD,
            ModifierKeyword.TRANSIENT_KEYWORD, ModifierKeyword.VOLATILE_KEYWORD, ModifierKeyword.SYNCHRONIZED_KEYWORD,
            ModifierKeyword.NATIVE_KEYWORD, ModifierKeyword.STRICTFP_KEYWORD));

    @Override
    public boolean visit(FieldDeclaration node) {
        if (isInterface(node.getParent())) {
            return removePublicStaticFinalModifiers(node);
        }
        if (isProtected(node.getModifiers()) && isFinalClass(node.getParent())) {
            return removeProtectedModifier(node);
        }
        return ensureModifiersOrder(node);
    }

    private boolean removePublicStaticFinalModifiers(FieldDeclaration node) {
        // Remove modifiers implied by the context
        boolean result= true;
        for (Modifier m : getModifiersOnly(modifiers(node))) {
            if (m.isPublic() || m.isStatic() || m.isFinal()) {
                ctx.getRefactorings().remove(m);
                result= false;
            }
        }
        return result;
    }

    private boolean isInterface(ASTNode node) {
        return node instanceof TypeDeclaration && ((TypeDeclaration) node).isInterface();
    }

    private boolean isFinalClass(ASTNode node) {
        return node instanceof TypeDeclaration && isFinal(((TypeDeclaration) node).getModifiers());
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (isInterface(node.getParent())) {
            // Remove modifiers implied by the context
            return removePublicAbstractModifiers(node);
        }
        int modifiers= node.getModifiers();
        if (isFinal(modifiers) && (isFinalClass(node.getParent()) || isPrivate(modifiers))) {
            return removeFinalModifier(modifiers(node));
        }
        if (isProtected(node.getModifiers()) && (node.isConstructor() ? isFinalClass(node.getParent())
                : isFinalClassWithoutInheritance(node.getParent()))) {
            return removeProtectedModifier(node);
        }
        return ensureModifiersOrder(node);
    }

    private boolean isFinalClassWithoutInheritance(ASTNode node) {
        if (node instanceof TypeDeclaration) {
            TypeDeclaration clazz= (TypeDeclaration) node;
            return isFinalClass(clazz) && clazz.superInterfaceTypes().isEmpty() && (clazz.getSuperclassType() == null
                    || hasType(clazz.getSuperclassType().resolveBinding(), Object.class.getCanonicalName()));
        }

        return false;
    }

    private boolean removeProtectedModifier(BodyDeclaration node) {
        for (Modifier modifier : getModifiersOnly(modifiers(node))) {
            if (modifier.isProtected()) {
                ctx.getRefactorings().remove(modifier);
                return false;
            }
        }
        return true;
    }

    private boolean removePublicAbstractModifiers(BodyDeclaration node) {
        boolean result= true;
        for (Modifier m : getModifiersOnly(modifiers(node))) {
            if (m.isPublic() || m.isAbstract()) {
                ctx.getRefactorings().remove(m);
                result= false;
            }
        }
        return result;
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration node) {
        return ensureModifiersOrder(node);
    }

    @Override
    public boolean visit(AnnotationTypeMemberDeclaration node) {
        return removePublicAbstractModifiers(node);
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        if (!removeStaticAbstractModifier(modifiers(node))) {
            return false;
        }

        return ensureModifiersOrder(node);
    }

    @Override
    public boolean visit(TryStatement node) {
        boolean result= true;
        for (VariableDeclarationExpression resource : resources(node)) {
            result&= removeFinalModifier(modifiers(resource));
        }
        return result;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        if (isInterface(node) && !removeStaticAbstractModifier(modifiers(node))) {
            return false;
        }

        return ensureModifiersOrder(node);
    }

    private boolean ensureModifiersOrder(BodyDeclaration node) {
        final List<IExtendedModifier> extendedModifiers= modifiers(node);
        final List<IExtendedModifier> reorderedModifiers= new ArrayList<IExtendedModifier>(extendedModifiers);
        Collections.sort(reorderedModifiers, new ModifierOrderComparator());

        if (!extendedModifiers.equals(reorderedModifiers)) {
            reorderModifiers(reorderedModifiers);
            return false;
        }

        return true;
    }

    private void reorderModifiers(final List<IExtendedModifier> reorderedModifiers) {
        final ASTBuilder b= ctx.getASTBuilder();

        for (int i= 0; i < reorderedModifiers.size(); i++) {
            IExtendedModifier m= reorderedModifiers.get(i);
            if (m.isModifier()) {
                ctx.getRefactorings().moveToIndex((Modifier) m, i, b.move((Modifier) m));
            } else {
                ctx.getRefactorings().moveToIndex((Annotation) m, i, b.move((Annotation) m));
            }
        }
    }

    private boolean removeStaticAbstractModifier(List<IExtendedModifier> modifiers) {
        boolean result= true;
        for (Modifier m : getModifiersOnly(modifiers)) {
            if (m.isStatic() || m.isAbstract()) {
                ctx.getRefactorings().remove(m);
                result= false;
            }
        }
        return result;
    }

    @Override
    public boolean visit(SingleVariableDeclaration node) {
        if (isInterface(node.getParent().getParent())) {
            return removeFinalModifier(modifiers(node));
        }
        return true;
    }

    private boolean removeFinalModifier(List<IExtendedModifier> modifiers) {
        boolean result= true;
        for (Modifier m : getModifiersOnly(modifiers)) {
            if (m.isFinal()) {
                ctx.getRefactorings().remove(m);
                result= false;
            }
        }
        return result;
    }

    private List<Modifier> getModifiersOnly(Collection<IExtendedModifier> modifiers) {
        final List<Modifier> results= new ArrayList<Modifier>();
        for (IExtendedModifier em : modifiers) {
            if (em.isModifier()) {
                results.add((Modifier) em);
            }
        }
        return results;
    }
}
