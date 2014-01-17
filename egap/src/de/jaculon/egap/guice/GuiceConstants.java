package de.jaculon.egap.guice;

import java.util.List;

import de.jaculon.egap.utils.ListUtils;

/**
 * Guice and Gin constants
 * 
 * https://code.google.com/p/google-guice/ 
 * https://code.google.com/p/google-gin/
 */
public class GuiceConstants {

    // @Inject
    public static final List<String> ANNOTATIONS_INJECT = ListUtils.immutableListOf("javax.inject.Inject",
            "com.google.inject.Inject");

    // @Singleton
    public static final String SINGLETON_SCOPE = "com.google.inject.Singleton";
    public static final String JAVAX_INJECT_SINGLETON = "javax.inject.Singleton";
    public static final List<String> SINGLETON_ANNOTATIONS = ListUtils.immutableListOf(SINGLETON_SCOPE,
            JAVAX_INJECT_SINGLETON);

    // @Named
    public static final String NAMED = "com.google.inject.name.Named";
    public static final String JAVAX_INJECT_NAMED = "javax.inject.Named";
    public static final List<String> NAMED_ANNOTATIONS = ListUtils.immutableListOf(NAMED, JAVAX_INJECT_NAMED);

    // Provider<>
    public static final String PROVIDER = "com.google.inject.Provider";
    public static final List<String> PROVIDER_ANNOTATIONS = ListUtils
            .immutableListOf(PROVIDER, "javax.inject.Provider");

    public static final String SCOPED_BINDING_BUILDER = "com.google.inject.binder.ScopedBindingBuilder";
    public static final String GIN_SCOPED_BINDING_BUILDER = "com.google.gwt.inject.client.binder.GinScopedBindingBuilder";
    public static final List<String> SCOPED_BINDING_BUILDERS = ListUtils.immutableListOf(SCOPED_BINDING_BUILDER,
            GIN_SCOPED_BINDING_BUILDER);

    public static final String LINKED_BINDING_BUILDER = "com.google.inject.binder.LinkedBindingBuilder";
    public static final String GIN_LINKED_BINDING_BUILDER = "com.google.gwt.inject.client.binder.GinLinkedBindingBuilder";
    public static final List<String> LINKED_BINDING_BUILDERS = ListUtils.immutableListOf(LINKED_BINDING_BUILDER,
            GIN_LINKED_BINDING_BUILDER);

    public static final String ANNOTATED_BINDING_BUILDER = "com.google.inject.binder.AnnotatedBindingBuilder";
    public static final String GIN_ANNOTATED_BINDING_BUILDER = "com.google.gwt.inject.client.binder.GinAnnotatedBindingBuilder";
    public static final List<String> ANNOTATED_BINDING_BUILDERS = ListUtils.immutableListOf(ANNOTATED_BINDING_BUILDER,
            GIN_ANNOTATED_BINDING_BUILDER);

    public static final String CONSTANT_BINDING_BUILDER = "com.google.inject.binder.ConstantBindingBuilder";
    public static final String GIN_CONSTANT_BINDING_BUILDER = "com.google.gwt.inject.client.binder.GinConstantBindingBuilder";
    public static final List<String> CONSTANT_BINDING_BUILDERS = ListUtils.immutableListOf(CONSTANT_BINDING_BUILDER,
            GIN_CONSTANT_BINDING_BUILDER);

    public static final String ANNOTATED_CONSTANT_BINDING_BUILDER = "com.google.inject.binder.AnnotatedConstantBindingBuilder";
    public static final String GIN_ANNOTATED_CONSTANT_BINDING_BUILDER = "com.google.gwt.inject.client.binder.GinAnnotatedConstantBindingBuilder";
    public static final List<String> ANNOTATED_CONSTANT_BINDING_BUILDERS = ListUtils.immutableListOf(
            ANNOTATED_CONSTANT_BINDING_BUILDER, GIN_ANNOTATED_CONSTANT_BINDING_BUILDER);

    public static final String ABSTRACT_MODULE = "com.google.inject.AbstractModule";
    public static final String ABSTRACT_GIN_MODULE = "com.google.gwt.inject.client.AbstractGinModule";
    public static final List<String> ABSTRACT_MODULES = ListUtils.immutableListOf(ABSTRACT_MODULE, ABSTRACT_GIN_MODULE);

    // MapBinder.newMapBinder(binder(), String.class, Snack.class);
    public static final String MAP_BINDER = "com.google.inject.multibindings.MapBinder";
    public static final String GIN_MAP_BINDER = "com.google.gwt.inject.client.multibindings.GinMapBinder";
    public static final List<String> MAP_BINDERS = ListUtils.immutableListOf(MAP_BINDER, GIN_MAP_BINDER);
    
    // Multibinder.newSetBinder(binder(), X.class);
    public static final String SET_BINDER = "com.google.inject.multibindings.Multibinder";
    public static final String GIN_SET_BINDER = "com.google.gwt.inject.client.multibindings.GinMultibinder";
    public static final List<String> SET_BINDERS = ListUtils.immutableListOf(SET_BINDER, GIN_SET_BINDER);
    
    
    public static final String ANNOTATION_ASSISTED = "com.google.inject.assistedinject.Assisted";
    public static final String PROVIDES = "com.google.inject.Provides";
    public static final String BINDING_ANNOTATION = "com.google.inject.BindingAnnotation";
    public static final String ASSISTEDINJECT_FACTORY_PROVIDER = "com.google.inject.assistedinject.FactoryProvider";
    public static final String TYPE_LITERAL = "com.google.inject.TypeLiteral";

}
