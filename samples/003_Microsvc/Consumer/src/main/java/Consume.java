
import cn.regionsoft.one.core.SYSEnvSetup;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.tutorial.service.LocalService;

public class Consume {

	public static void main(String[] args) throws Exception {
		SYSEnvSetup.setUp();
		LocalService localService = SystemContext.getInstance().getManagedBean(LocalService.class);
		String result = localService.invokeRemoveService();
		System.out.println(result);
	}

}
