package com.cuieney.srouter_compiler.processor;

import com.cuieney.srouter_annotation.annotation.Autowired;
import com.cuieney.srouter_annotation.enums.TypeKind;
import com.cuieney.srouter_compiler.utils.Consts;
import com.cuieney.srouter_compiler.utils.Logger;
import com.cuieney.srouter_compiler.utils.TypeUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.cuieney.srouter_compiler.utils.Consts.ANNOTATION_TYPE_AUTOWIRED;
import static com.cuieney.srouter_compiler.utils.Consts.ISYRINGE;
import static com.cuieney.srouter_compiler.utils.Consts.KEY_MODULE_NAME;
import static com.cuieney.srouter_compiler.utils.Consts.METHOD_INJECT;
import static com.cuieney.srouter_compiler.utils.Consts.NAME_OF_AUTOWIRED;
import static com.cuieney.srouter_compiler.utils.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Processor used to create autowired helper
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/2/20 下午5:56
 */
@AutoService(Processor.class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({ANNOTATION_TYPE_AUTOWIRED})
public class AutowiredProcessor extends AbstractProcessor {
    private Filer mFiler;       // File util, write class file into disk.
    private Logger logger;
    private Types types;
    private TypeUtils typeUtils;
    private Elements elements;
    private Map<TypeElement, List<Element>> parentAndChild = new HashMap<>();   // Contain field need autowired and his super class.
    private static final ClassName ARouterClass = ClassName.get("com.cuieney.srouter_api.launcher", "SRouter");
    private static final ClassName JsonClass = ClassName.get("com.alibaba.fastjson", "JSON");

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mFiler = processingEnv.getFiler();                  // Generate class.
        types = processingEnv.getTypeUtils();            // Get type utils.
        elements = processingEnv.getElementUtils();      // Get class meta.

        typeUtils = new TypeUtils(types, elements);

        logger = new Logger(processingEnv.getMessager());   // Package the log utils.

        logger.info(">>> AutowiredProcessor init. <<<");
        logger.info("init");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        logger.info("process");
        if (CollectionUtils.isNotEmpty(set)) {
            try {
                logger.info(">>> Found autowired field, start... <<<");
                categories(roundEnvironment.getElementsAnnotatedWith(Autowired.class));
                generateHelper();

            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }

        return false;
    }

    private void generateHelper() throws IOException, IllegalAccessException {
        TypeElement type_ISyringe = elements.getTypeElement(ISYRINGE);
        TypeMirror iProvider = elements.getTypeElement(Consts.IPROVIDER).asType();
        TypeMirror activityTm = elements.getTypeElement(Consts.ACTIVITY).asType();
        TypeMirror fragmentTm = elements.getTypeElement(Consts.FRAGMENT).asType();
        TypeMirror fragmentTmV4 = elements.getTypeElement(Consts.FRAGMENT_V4).asType();

        // Build input param name.
        ParameterSpec objectParamSpec = ParameterSpec.builder(TypeName.OBJECT, "target").build();

        if (MapUtils.isNotEmpty(parentAndChild)) {
            for (Map.Entry<TypeElement, List<Element>> entry : parentAndChild.entrySet()) {
                // Build method : 'inject'
                MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder(METHOD_INJECT)
                        .addAnnotation(Override.class)
                        .addModifiers(PUBLIC)
                        .addParameter(objectParamSpec);

                TypeElement parent = entry.getKey();
                List<Element> childs = entry.getValue();

                String qualifiedName = parent.getQualifiedName().toString();
                String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
                String fileName = parent.getSimpleName() + NAME_OF_AUTOWIRED;

                logger.info(">>> Start process " + childs.size() + " field in " + parent.getSimpleName() + " ... <<<");

                injectMethodBuilder.addStatement("$T substitute = ($T)target", ClassName.get(parent), ClassName.get(parent));

                // Generate method body, start inject.
                for (Element element : childs) {
                    Autowired fieldConfig = element.getAnnotation(Autowired.class);
                    String fieldName = element.getSimpleName().toString();
                    if (types.isSubtype(element.asType(), iProvider)) {  // It's provider
                        if ("".equals(fieldConfig.name())) {    // User has not set service path, then use byType.

                            // Getter
                            injectMethodBuilder.addStatement(
                                    "substitute." + fieldName + " = $T.getInstance().navigation($T.class)",
                                    ARouterClass,
                                    ClassName.get(element.asType())
                            );
                        } else {    // use byName
                            // Getter
                            injectMethodBuilder.addStatement(
                                    "substitute." + fieldName + " = ($T)$T.getInstance().build($S).navigation();",
                                    ClassName.get(element.asType()),
                                    ARouterClass,
                                    fieldConfig.name()
                            );
                        }

                        // Validater
                        if (fieldConfig.required()) {
                            injectMethodBuilder.beginControlFlow("if (substitute." + fieldName + " == null)");
                            injectMethodBuilder.addStatement(
                                    "throw new RuntimeException(\"The field '" + fieldName + "' is null, in class '\" + $T.class.getName() + \"!\")", ClassName.get(parent));
                            injectMethodBuilder.endControlFlow();
                        }
                    } else {    // It's normal intent value
                        String statment = "substitute." + fieldName + " = substitute.";
                        boolean isActivity = false;
                        if (types.isSubtype(parent.asType(), activityTm)) {  // Activity, then use getIntent()
                            isActivity = true;
                            statment += "getIntent().";
                        } else if (types.isSubtype(parent.asType(), fragmentTm) || types.isSubtype(parent.asType(), fragmentTmV4)) {   // Fragment, then use getArguments()
                            statment += "getArguments().";
                        } else {
                            throw new IllegalAccessException("The field [" + fieldName + "] need autowired from intent, its parent must be activity or fragment!");
                        }

                        statment = buildStatement(statment, typeUtils.typeExchange(element), isActivity);
                        if (statment.startsWith("$T.")) {   // Not mortals
                            injectMethodBuilder.addStatement(
                                    "substitute." + fieldName + " = " + statment,
                                    JsonClass,
                                    (StringUtils.isEmpty(fieldConfig.name()) ? fieldName : fieldConfig.name()),
                                    ClassName.get(element.asType())
                            );
                        } else {
                            injectMethodBuilder.addStatement(statment, StringUtils.isEmpty(fieldConfig.name()) ? fieldName : fieldConfig.name());
                        }

                        // Validater
                        if (fieldConfig.required() && !element.asType().getKind().isPrimitive()) {  // Primitive wont be check.
                            injectMethodBuilder.beginControlFlow("if (substitute." + fieldName + " == null)");
                            injectMethodBuilder.addStatement(
                                    "throw new RuntimeException(\"The field '" + fieldName + "' is null, in class '\" + $T.class.getName() + \"!\")", ClassName.get(parent));
                            injectMethodBuilder.endControlFlow();
                        }
                    }
                }

                // Generate autowired helper
                JavaFile.builder(packageName,
                        TypeSpec.classBuilder(fileName)
                                .addJavadoc(WARNING_TIPS)
                                .addSuperinterface(ClassName.get(type_ISyringe))
                                .addModifiers(PUBLIC)
                                .addMethod(injectMethodBuilder.build())
                                .build()
                ).build().writeTo(mFiler);

                logger.info(">>> " + parent.getSimpleName() + " has been processed, " + fileName + " has been generated. <<<");
            }

            logger.info(">>> Autowired processor stop. <<<");
        }
    }

    private String buildStatement(String statment, int type, boolean isActivity) {
        if (type == TypeKind.BOOLEAN.ordinal()) {
            statment += (isActivity ? ("getBooleanExtra($S, false)") : ("getBoolean($S)"));
        } else if (type == TypeKind.BYTE.ordinal()) {
            statment += (isActivity ? ("getByteExtra($S, (byte) 0)") : ("getByte($S)"));
        } else if (type == TypeKind.SHORT.ordinal()) {
            statment += (isActivity ? ("getShortExtra($S, (short) 0)") : ("getShort($S)"));
        } else if (type == TypeKind.INT.ordinal()) {
            statment += (isActivity ? ("getIntExtra($S, 0)") : ("getInt($S)"));
        } else if (type == TypeKind.LONG.ordinal()) {
            statment += (isActivity ? ("getLongExtra($S, 0)") : ("getLong($S)"));
        } else if (type == TypeKind.FLOAT.ordinal()) {
            statment += (isActivity ? ("getFloatExtra($S, 0)") : ("getFloat($S)"));
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            statment += (isActivity ? ("getDoubleExtra($S, 0)") : ("getDouble($S)"));
        } else if (type == TypeKind.STRING.ordinal()) {
            statment += (isActivity ? ("getStringExtra($S)") : ("getString($S)"));
        } else if (type == TypeKind.PARCELABLE.ordinal()) {
            statment += (isActivity ? ("getParcelableExtra($S)") : ("getParcelable($S)"));
        } else if (type == TypeKind.OBJECT.ordinal()) {
            statment = "$T.parseObject(" + (isActivity ? "substitute.getIntent()." : "getArguments(). ") + "getStringExtra($S), $T.class)";
        }

        return statment;
    }

    /**
     * Categories field, find his papa.
     *
     * @param elements Field need autowired
     */
    private void categories(Set<? extends Element> elements) throws IllegalAccessException {
        if (CollectionUtils.isNotEmpty(elements)) {
            for (Element element : elements) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                if (element.getModifiers().contains(Modifier.PRIVATE)) {
                    throw new IllegalAccessException("The autowired fields CAN NOT BE 'private'!!! please check field ["
                            + element.getSimpleName() + "] in class [" + enclosingElement.getQualifiedName() + "]");
                }

                if (parentAndChild.containsKey(enclosingElement)) { // Has categries
                    parentAndChild.get(enclosingElement).add(element);
                } else {
                    List<Element> childs = new ArrayList<>();
                    childs.add(element);
                    parentAndChild.put(enclosingElement, childs);
                }
            }

            logger.info("categories finished.");
        }
    }
}
