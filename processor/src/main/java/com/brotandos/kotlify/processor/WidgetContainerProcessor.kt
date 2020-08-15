package com.brotandos.kotlify.processor

import com.brotandos.kotlify.annotation.WidgetContainer
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
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
import javax.lang.model.element.VariableElement
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
            if (it.kind == ElementKind.CLASS) return@forEach constructWidgetContainer(it)
            processingEnv.printError("Only classes can be annotated")
            return true
        }
        return false
    }

    private fun constructWidgetContainer(element: Element) {
        processingEnv.printNote("PROCESS ANNOTATION")
        val parentClassName = element.simpleName.toString()
        val packageValue = processingEnv.elementUtils.getPackageOf(element).toString()
        val className = "${parentClassName}Itself"

        if (element !is TypeElement) throw IllegalStateException("element must implement javax.lang.model.element.TypeElement")

        val layoutGeneric = getLayoutGeneric(element)

        val layoutParamsGeneric = getLayoutParamsGeneric(element)

        val constructorElement = getConstructorElement(element)

        val sizeVal = constructorElement.parameters.first() ?: return

        val fileBuilder = FileSpec.builder(packageValue, className)

        val contextClass = ClassName("android.content", "Context")

        val classBuilder = TypeSpec.classBuilder(className)
            .superclass(buildSuperclass(element, packageValue, layoutGeneric, layoutParamsGeneric))
            .addSuperclassConstructorParameter("%N", sizeVal.simpleName)
            .primaryConstructor(buildConstructor(sizeVal))
            .addFunction(buildCreateViewFuction(contextClass, layoutGeneric))
            .addFunction(buildGetChildLayoutParamsFunction(layoutGeneric, layoutParamsGeneric))

        val file = fileBuilder.addType(classBuilder.build()).build()
        val kaptKotlinGeneratedDir = processingEnv.options[Utils.KAPT_KOTLIN_GENERATED_OPTION_NAME]
            ?: throw IllegalStateException("kapt generated directory name shouldn't be null")
        file.writeTo(File(kaptKotlinGeneratedDir))
    }

    private fun getLayoutGeneric(element: TypeElement) = (element
        .typeParameters[Utils.WIDGET_CONTAINER_GENERIC_LAYOUT]
        .bounds
        .firstOrNull()
        ?: throw IllegalStateException("element must have generic bounds of LayoutParams"))
        as? DeclaredType
        ?: throw IllegalStateException("type must implement javax.lang.model.type.DeclaredType")

    private fun getLayoutParamsGeneric(element: TypeElement) = (element
        .typeParameters[Utils.WIDGET_CONTAINER_GENERIC_LAYOUT_PARAMS]
        .bounds
        .firstOrNull()
        ?: throw IllegalStateException("element must have generic bounds of LayoutParams"))
        as? DeclaredType
        ?: throw IllegalStateException("type must implement javax.lang.model.type.DeclaredType")

    private fun getConstructorElement(element: TypeElement) =
        (element.enclosedElements.find { it.kind == ElementKind.CONSTRUCTOR }
            ?: throw IllegalStateException("element must have constructor"))
            as? ExecutableElement
            ?: throw IllegalStateException("constructor must implement javax.lang.model.element.ExecutableElement")

    private fun buildConstructor(sizeVal: VariableElement) = FunSpec.constructorBuilder()
        .addParameter(sizeVal.simpleName.toString(), sizeVal.asType().asTypeName())
        .build()

    private fun buildCreateViewFuction(contextClass: ClassName, layoutGeneric: DeclaredType) =
        FunSpec.builder("createView")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(ParameterSpec.builder("context", contextClass).build())
            .returns(layoutGeneric.asTypeName())
            .addStatement("return %N(context)", layoutGeneric.asElement().simpleName)
            .build()

    private fun buildGetChildLayoutParamsFunction(layoutGeneric: DeclaredType, layoutParamsGeneric: DeclaredType) =
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

    private fun buildSuperclass(
        element: TypeElement,
        packageValue: String,
        layoutGeneric: DeclaredType,
        layoutParamsGeneric: DeclaredType
    ): ParameterizedTypeName = ClassName(packageValue, element.simpleName.toString())
        .plusParameter(layoutGeneric.asTypeName())
        .plusParameter(layoutParamsGeneric.asTypeName())
}