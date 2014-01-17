package de.jaculon.egap.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WildcardType;

import de.jaculon.egap.guice.GuiceConstants;
import de.jaculon.egap.source_builder.JavaCodeBuilder;

public class TypeUtils {

    public static boolean isMapBinderType(Type type) {
        return isTypeDeclarationTypeKindOfAny(type, GuiceConstants.MAP_BINDERS);
    }

    public static boolean isSetBinderType(Type type) {
        return isTypeDeclarationTypeKindOfAny(type, GuiceConstants.SET_BINDERS);
    }
    

    public static boolean isTypeDeclarationTypeKindOf(Type type, String typeFullyQualifiedName) {
        String typeBindingQualifiedName = getTypeOfTypeDeclaration(type);
        return typeBindingQualifiedName.equals(typeFullyQualifiedName);
    }

    public static boolean isTypeDeclarationTypeKindOfAny(Type type, List<String> typeFullyQualifiedNames) {
        for (String fqn : typeFullyQualifiedNames) {
            if (isTypeDeclarationTypeKindOf(type, fqn)) {
                return true;
            }
        }
        return false;
    }
    
    public static String getTypeOfTypeDeclaration(Type type) {
        ITypeBinding typeBinding = type.resolveBinding();
        ITypeBinding typeDeclaration = typeBinding.getTypeDeclaration();
        String typeBindingQualifiedName = typeDeclaration.getQualifiedName();
        return typeBindingQualifiedName;
    }

    /**
     * Returns the type declaration for the given type as String.
     * 
     * Note: The types are <b>not</b> fully qualified.
     * 
     * <h5>Example:</h5>
     * 
     * <pre>
     * <code>
     * getTypeDeclarationAsString(providerType);
     * "Provider<Integer>"
     * </code>
     * </pre>
     * 
     * @param type
     *            the type
     * @return the type declaration as String
     */
    public static String getTypeDeclarationAsString(Type type) {
        StringBuilder stringBuilder = new StringBuilder();
        appendTypeDeclarationAsString(type, stringBuilder);
        return stringBuilder.toString();
    }

    public static void appendTypeDeclarationAsString(Type type, StringBuilder sb) {

        if (type.isSimpleType()) {
            SimpleType simpleType = (SimpleType) type;
            Name name = simpleType.getName();
            sb.append(name.getFullyQualifiedName());
        } else if (type.isQualifiedType()) {
            QualifiedType qualifiedType = (QualifiedType) type;
            Name name = qualifiedType.getName();
            sb.append(name.getFullyQualifiedName());
        } else if (type.isArrayType()) {
            ArrayType arrayType = (ArrayType) type;
            Type arrayElementType = arrayType.getElementType();
            appendTypeDeclarationAsString(arrayElementType, sb);
            sb.append("[]");
        } else if (type.isParameterizedType()) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            SimpleType typeOfParemeterizedType = (SimpleType) parameterizedType.getType();
            @SuppressWarnings("unchecked")
            List<Type> typeArguments = parameterizedType.typeArguments();

            Name name = typeOfParemeterizedType.getName();
            String fullyQualifiedName = name.getFullyQualifiedName();
            sb.append(fullyQualifiedName);
            sb.append("<");

            int j = 0;
            for (Type typeArg : typeArguments) {
                if (j > 0) {
                    sb.append(JavaCodeBuilder.KOMMATA);
                }
                appendTypeDeclarationAsString(typeArg, sb);
            }

            sb.append(">");
        } else if (type.isWildcardType()) {
            WildcardType wildcardType = (WildcardType) type;
            sb.append(wildcardType.toString());
        } else if (type.isPrimitiveType()) {
            PrimitiveType primitiveType = (PrimitiveType) type;
            Code primitiveTypeCode = primitiveType.getPrimitiveTypeCode();
            String primitiveTypeName = primitiveTypeCode.toString();
            sb.append(primitiveTypeName);
        } else {
            throw new RuntimeException("Unsupported type '" + type + "'");
        }
    }

    public static String resolveQualifiedName(Type type) {
        ITypeBinding typeBinding = type.resolveBinding();
        String qualifiedName = typeBinding.getQualifiedName();
        return qualifiedName;
    }

    public static String wrapInType(List<Type> list, String wrapType) {

        StringBuffer sb = new StringBuffer();
        sb.append(wrapType + "<");
        int i = 0;
        for (Type type : list) {
            if (i++ > 0) {
                sb.append(",");
            }
            ITypeBinding resolveBinding = type.resolveBinding();
            sb.append(resolveBinding.getQualifiedName());
        }
        sb.append(">");

        return sb.toString();
    }

}
