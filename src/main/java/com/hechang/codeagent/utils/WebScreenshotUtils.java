package com.hechang.codeagent.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hechang.codeagent.exception.BusinessException;
import com.hechang.codeagent.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网页截图工具类
 * 采用 ThreadLocal 模式管理 WebDriver，支持多线程并发截图
 */
@Slf4j
public class WebScreenshotUtils {

    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 900;

    private static final String IMAGE_SUFFIX = ".png";

    private static final String COMPRESS_SUFFIX = "_compressed.jpg";
    
    /**
     * 图片压缩质量（0.3 = 30% 质量，平衡文件大小和清晰度）
     */
    private static final float COMPRESSION_QUALITY = 0.3f;

    private static final long PAGE_LOAD_TIMEOUT = 180;
    private static final long IMPLICIT_WAIT_TIMEOUT = 180;
    private static final long EXPLICIT_WAIT_TIMEOUT = 180;
    
    /**
     * 额外等待动态内容加载时间（毫秒）
     */
    private static final long EXTRA_WAIT_TIME = 2000;

    /**
     * ThreadLocal 存储每个线程的 WebDriver 实例
     * 确保每个线程使用独立的浏览器实例，避免并发冲突
     */
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    
    /**
     * 记录所有创建的 WebDriver 实例，用于应用关闭时统一清理
     * Key: WebDriver 实例, Value: 创建时间戳
     */
    private static final ConcurrentHashMap<WebDriver, Long> allDrivers = new ConcurrentHashMap<>();

    /**
     * 获取当前线程的 WebDriver 实例
     * 如果当前线程没有 WebDriver，则创建一个新的实例
     *
     * @return WebDriver 实例
     */
    private static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            // 当前线程首次请求，初始化 Chrome 驱动
            driver = initChromeDriver();
            driverThreadLocal.set(driver);
            // 记录到全局管理器中，便于后续统一清理
            allDrivers.put(driver, System.currentTimeMillis());
            log.debug("为新线程创建 WebDriver 实例，当前线程: {}", Thread.currentThread().getName());
        }
        return driver;
    }

    /**
     * 清理当前线程的 WebDriver 资源
     * 应在任务完成后调用，避免资源泄漏
     */
    private static void cleanupDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                log.debug("清理线程 WebDriver 实例，当前线程: {}", Thread.currentThread().getName());
            } catch (Exception e) {
                log.warn("清理 WebDriver 时出现异常", e);
            } finally {
                // 从 ThreadLocal 中移除
                driverThreadLocal.remove();
                // 从全局管理器中移除
                allDrivers.remove(driver);
            }
        }
    }

    /**
     * 生成网页截图
     *
     * @param webUrl 要截图的网址
     * @return 压缩后的截图文件路径，失败返回 null
     */
    public static String saveWebPageScreenshot(String webUrl) {
        // 非空校验
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页截图失败，url为空");
            return null;
        }
        
        WebDriver driver = null;
        try {
            // 获取当前线程的 WebDriver 实例
            driver = getDriver();
            // 创建临时目录
            String rootPath = System.getProperty("user.dir") + "/src/main/tmp/screenshots/" + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);
            // 原始图片保存路径
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;
            // 访问网页
            driver.get(webUrl);
            // 等待网页加载完成
            waitForPageLoad(driver);
            // 执行截图
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            // 保存原始图片
            saveImage(screenshotBytes, imageSavePath);
            log.info("原始截图保存成功：{}", imageSavePath);
            // 压缩图片
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESS_SUFFIX;
            compressImage(imageSavePath, compressedImagePath);
            log.info("压缩图片保存成功：{}", compressedImagePath);
            // 删除原始图片
            FileUtil.del(imageSavePath);
            
            return compressedImagePath;
        } catch (Exception e) {
            log.error("网页截图失败：{}", webUrl, e);
            return null;
        } finally {
            // 重要：清理当前线程的 WebDriver 资源，避免内存泄漏
            // 如果需要复用 WebDriver（频繁截图场景），可以注释掉这一行
            cleanupDriver();
        }
    }

    /**
     * 初始化 Chrome 浏览器驱动
     *
     * @return 配置好的 WebDriver 实例
     */
    private static WebDriver initChromeDriver() {
        try {
            // 自动下载并配置 ChromeDriver（根据系统自动匹配版本）
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = getChromeOptions(WebScreenshotUtils.DEFAULT_WIDTH, WebScreenshotUtils.DEFAULT_HEIGHT);

            // 创建 ChromeDriver 实例
            WebDriver driver = new ChromeDriver(options);
            
            // 设置页面加载超时时间
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT));
            
            // 设置隐式等待时间（查找元素时的最大等待时间）
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_TIMEOUT));
            
            log.debug("Chrome WebDriver 初始化成功，窗口大小: {}x{}", WebScreenshotUtils.DEFAULT_WIDTH, WebScreenshotUtils.DEFAULT_HEIGHT);
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    private static ChromeOptions getChromeOptions(int width, int height) {
        ChromeOptions options = new ChromeOptions();

        // 无头模式：浏览器在后台运行，不显示图形界面
        options.addArguments("--headless");

        // 禁用 GPU 加速（避免某些环境下的兼容性问题）
        options.addArguments("--disable-gpu");

        // 禁用沙盒模式（Docker/容器环境必需）
        // 配合 --disable-dev-shm-usage 可确保在容器环境中正常运行
        options.addArguments("--no-sandbox");

        // 禁用 /dev/shm 共享内存使用（Docker 环境常见问题解决）
        options.addArguments("--disable-dev-shm-usage");

        // 设置浏览器窗口大小
        options.addArguments(String.format("--window-size=%d,%d", width, height));

        // 禁用浏览器扩展（提升性能和稳定性）
        options.addArguments("--disable-extensions");

        // 设置用户代理字符串（模拟真实浏览器）
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        return options;
    }

    /**
     * 保存图片到文件系统
     *
     * @param imageBytes 图片字节数组
     * @param imagePath  保存路径
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败：{}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片（降低质量以减小文件大小）
     *
     * @param originImagePath     原始图片路径
     * @param compressedImagePath 压缩后图片路径
     */
    private static void compressImage(String originImagePath, String compressedImagePath) {
        try {
            ImgUtil.compress(
                    FileUtil.file(originImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("压缩图片失败：{} -> {}", originImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面完全加载完成
     * 包括静态资源和动态 JavaScript 内容
     *
     * @param driver WebDriver 实例
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            // 创建显式等待对象
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_TIMEOUT));
            
            // 等待 document.readyState 变为 "complete"，表示页面基本加载完成
            wait.until(waitDriver -> Objects.equals(
                    ((JavascriptExecutor) waitDriver).executeScript("return document.readyState"), 
                    "complete"
            ));
            
            // 额外等待一段时间，确保 AJAX、动画等动态内容加载完成
            Thread.sleep(EXTRA_WAIT_TIME);
            
            log.debug("页面加载完成");
        } catch (Exception e) {
            // 即使等待超时，也继续执行截图（可能部分加载的内容也可用）
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }
    }

    /**
     * 应用关闭时清理所有 WebDriver 资源
     * 由 Spring 容器在销毁 Bean 时自动调用
     */
    @PreDestroy
    public void destroy() {
        log.info("开始清理所有 WebDriver 实例，总数: {}", allDrivers.size());
        
        // 遍历所有创建的 WebDriver 实例并关闭
        allDrivers.keySet().forEach(driver -> {
            try {
                driver.quit();
                log.debug("已关闭一个 WebDriver 实例");
            } catch (Exception e) {
                log.warn("关闭 WebDriver 时出现异常", e);
            }
        });
        
        // 清空全局管理器
        allDrivers.clear();
        
        // 清理 ThreadLocal（虽然应用关闭时会回收，但显式清理是好习惯）
        driverThreadLocal.remove();
        
        log.info("所有 WebDriver 实例清理完成");
    }
}
