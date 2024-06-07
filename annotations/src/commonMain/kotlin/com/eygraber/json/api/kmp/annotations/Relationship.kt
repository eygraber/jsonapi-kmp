package com.eygraber.json.api.kmp.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
public annotation class Relationship(val value: String)
