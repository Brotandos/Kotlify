package com.brotandos.kotlify.processor

import com.brotandos.kotlify.annotation.WidgetContainer
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

@Suppress("unused")
@AutoService(Processor::class)
@SupportedOptions(Utils.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class WidgetContainerProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(WidgetContainer::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        roundEnv.getElementsAnnotatedWith(WidgetContainer::class.java).forEach {
            if (it.kind == ElementKind.CLASS) return@forEach processAnnotation(it)
            processingEnv.printError("Only classes can be annotated")
            return true
        }
        return false
    }

    private fun processAnnotation(element: Element) {
        processingEnv.printNote("PROCESS ANNOTATION")
        val className = element.simpleName.toString()
        val packageValue = processingEnv.elementUtils.getPackageOf(element).toString()
        val fileName = "${className}Itself"

        if (element !is TypeElement) throw IllegalStateException("element must implement javax.lang.model.element.TypeElement")

        val layoutGeneric = (element
            .typeParameters[Utils.WIDGET_CONTAINER_GENERIC_LAYOUT]
            .bounds
            .firstOrNull()
            ?: throw IllegalStateException("element must have generic bounds of LayoutParams"))
                as? DeclaredType
            ?: throw IllegalStateException("type must implement javax.lang.model.type.DeclaredType")

        val layoutParamsGeneric = (element
            .typeParameters[Utils.WIDGET_CONTAINER_GENERIC_LAYOUT_PARAMS]
            .bounds
            .firstOrNull()
            ?: throw IllegalStateException("element must have generic bounds of LayoutParams"))
                as? DeclaredType
            ?: throw IllegalStateException("type must implement javax.lang.model.type.DeclaredType")

        val constructorElement =
            (element.enclosedElements.find { it.kind == ElementKind.CONSTRUCTOR }
                ?: throw IllegalStateException("element must have constructor"))
                    as? ExecutableElement
                ?: throw IllegalStateException("constructor must implement javax.lang.model.element.ExecutableElement")

        val sizeVal = constructorElement.parameters.first() ?: return

        val superclass = ClassName(packageValue, element.simpleName.toString())
            .plusParameter(layoutGeneric.asTypeName())
            .plusParameter(layoutParamsGeneric.asTypeName())

        val fileBuilder = FileSpec.builder(packageValue, fileName)

        val contextClass = ClassName("android.content", "Context")

        val classBuilder = TypeSpec.classBuilder(fileName)
            .superclass(superclass)
            .addSuperclassConstructorParameter("%N", sizeVal.simpleName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(sizeVal.simpleName.toString(), sizeVal.asType().asTypeName())
                    .build()
            )
            .addFunction(
                FunSpec.builder("createView")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(ParameterSpec.builder("context", contextClass).build())
                    .returns(layoutGeneric.asTypeName())
                    .addStatement("return %N(context)", layoutGeneric.asElement().simpleName)
                    .build()
            )
            .addFunction(
                FunSpec.builder("getChildLayoutParams")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(ParameterSpec.builder("width", Int::class).build())
                    .addParameter(ParameterSpec.builder("height", Int::class).build())
                    .returns(layoutParamsGeneric.asTypeName())
                    .addStatement(
                        "return %N.%N(width, height)",
                        layoutGeneric.asElement().simpleName,
                        layoutParamsGeneric.asElement().simpleName
                    )
                    .build()
            )

        val file = fileBuilder.addType(classBuilder.build()).build()
        val kaptKotlinGeneratedDir = processingEnv.options[Utils.KAPT_KOTLIN_GENERATED_OPTION_NAME]
            ?: throw IllegalStateException("kapt generated directory name shouldn't be null")
        file.writeTo(File(kaptKotlinGeneratedDir))
    }
}