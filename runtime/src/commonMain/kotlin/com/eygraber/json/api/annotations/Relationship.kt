package com.eygraber.json.api.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
public annotation class Relationship(val value: String)
