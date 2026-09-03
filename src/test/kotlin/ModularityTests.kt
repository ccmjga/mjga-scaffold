import com.zl.mjga.ApplicationService
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

@Tag("architecture")
class ModularityTests {
    @Test
    fun `declared capability graph is acyclic and uses named interfaces`() {
        ApplicationModules.of(ApplicationService::class.java).verify()
    }
}
