package com.zl.mjga.platform.events

/** Trusted framework-neutral identity and trace context propagated across Capability Interfaces. */
@org.springframework.modulith.NamedInterface("api")
data class InvocationContext(
    val actor: String,
    val tenant: String?,
    val correlationId: String,
    val causationId: String?,
    val kind: InvocationKind,
) {
    init {
        require(actor.isNotBlank()) { "actor must not be blank" }
        require(correlationId.isNotBlank()) { "correlationId must not be blank" }
    }

    enum class InvocationKind { USER, SYSTEM, SCHEDULED, EVENT }
}
