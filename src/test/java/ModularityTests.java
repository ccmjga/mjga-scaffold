import com.zl.mjga.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModularityTests {

  @Test
  public void applicationModules() {
    ApplicationModules modules = ApplicationModules.of(ApplicationService.class);
    modules.forEach(System.out::println);
    modules.verify();
  }

  @Test
  void createModuleDocumentation() {
    ApplicationModules modules = ApplicationModules.of(ApplicationService.class);
    new Documenter(modules).writeDocumentation().writeIndividualModulesAsPlantUml();
  }
}
