package com.healthtrail.admin.config;


import com.healthtrail.admin.HealthTrailAdminApplication;
import com.healthtrail.common.config.HealthTrailConfig;
import com.healthtrail.common.constant.Constants.UploadSubDir;
import java.io.File;
import javax.annotation.Resource;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = HealthTrailAdminApplication.class)
@RunWith(SpringRunner.class)
public class HealthTrailConfigTest {

    @Resource
    private HealthTrailConfig config;

    @Test
    public void testConfig() {
        String fileBaseDir = "D:\\healthtrail\\profile";

        Assertions.assertEquals("HealthTrail", config.getName());
        Assertions.assertEquals("1.8.0", config.getVersion());
        Assertions.assertEquals("2022", config.getCopyrightYear());
        Assertions.assertFalse(config.isDemoEnabled());
        Assertions.assertEquals(fileBaseDir, HealthTrailConfig.getFileBaseDir());
        Assertions.assertFalse(HealthTrailConfig.isAddressEnabled());
        Assertions.assertEquals("math", HealthTrailConfig.getCaptchaType());
        Assertions.assertEquals("math", HealthTrailConfig.getCaptchaType());
        Assertions.assertEquals(fileBaseDir + "\\import",
            HealthTrailConfig.getFileBaseDir() + File.separator + UploadSubDir.IMPORT_PATH);
        Assertions.assertEquals(fileBaseDir + "\\avatar",
            HealthTrailConfig.getFileBaseDir() + File.separator + UploadSubDir.AVATAR_PATH);
        Assertions.assertEquals(fileBaseDir + "\\download",
            HealthTrailConfig.getFileBaseDir() + File.separator + UploadSubDir.DOWNLOAD_PATH);
        Assertions.assertEquals(fileBaseDir + "\\upload",
            HealthTrailConfig.getFileBaseDir() + File.separator + UploadSubDir.UPLOAD_PATH);
    }

}
