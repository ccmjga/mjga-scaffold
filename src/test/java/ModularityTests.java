import com.zl.mjga.ApplicationService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

@Tag("architecture")
class ModularityTests {

  @Test
  void declaredCapabilityGraphIsAcyclicAndUsesNamedInterfaces() {
    ApplicationModules modules = ApplicationModules.of(ApplicationService.class);
    modules.verify();
  }
}
