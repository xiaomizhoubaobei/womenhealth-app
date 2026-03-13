# CI/CD 签名配置指南

## 问题背景
`local.properties` 文件通常被 `.gitignore` 忽略以保护敏感信息（如密钥库密码），但这会导致云端构建无法获取签名配置。

## 解决方案
采用多层级配置策略：**命令行参数 > 环境变量 > local.properties 文件**

## 配置方法

### 1. 环境变量方式（推荐用于 CI/CD）

在 CI/CD 平台设置以下环境变量：

```bash
# Keystore 文件路径（相对于项目根目录或绝对路径）
KEYSTORE_FILE=keystores/release.keystore

# Keystore 密码
KEYSTORE_PASSWORD=your_keystore_password

# 密钥别名
KEY_ALIAS=your_key_alias

# 密钥密码
KEY_PASSWORD=your_key_password
```

#### GitHub Actions 示例
```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    env:
      KEYSTORE_FILE: ${{ github.workspace }}/keystores/release.keystore
      KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
      KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
      KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
    steps:
      - uses: actions/checkout@v3
      - name: Setup Android SDK
        uses: android-actions/setup-android@v2
      - name: Build Release APK
        run: ./gradlew assembleRelease
```

#### GitLab CI 示例
```yaml
variables:
  KEYSTORE_FILE: "$CI_PROJECT_DIR/keystores/release.keystore"
  KEYSTORE_PASSWORD: $KEYSTORE_PASSWORD
  KEY_ALIAS: $KEY_ALIAS
  KEY_PASSWORD: $KEY_PASSWORD

build:
  stage: build
  script:
    - ./gradlew assembleRelease
  artifacts:
    paths:
      - app/build/outputs/apk/release/
```

#### Jenkins 示例
```groovy
pipeline {
    environment {
        KEYSTORE_FILE = "${WORKSPACE}/keystores/release.keystore"
        KEYSTORE_PASSWORD = credentials('keystore-password')
        KEY_ALIAS = credentials('key-alias')
        KEY_PASSWORD = credentials('key-password')
    }
    stages {
        stage('Build') {
            steps {
                sh './gradlew assembleRelease'
            }
        }
    }
}
```

### 2. 命令行参数方式（适用于临时构建）

```bash
./gradlew assembleRelease \
  -PKEYSTORE_FILE=../keystores/release.keystore \
  -PKEYSTORE_PASSWORD=your_keystore_password \
  -PKEY_ALIAS=your_key_alias \
  -PKEY_PASSWORD=your_key_password
```

### 3. local.properties 文件方式（适用于本地开发）

复制模板文件并填写实际值：

```bash
cp local.properties.example local.properties
```

编辑 `local.properties`：

```properties
# Path to your Android SDK
sdk.dir=/path/to/your/android/sdk

# Keystore configuration
KEYSTORE_FILE=../keystores/release.keystore
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

## v1-v4 签名说明

构建脚本已配置支持所有签名版本：

- **V1 (Jar signature)**: 传统签名方案，所有 Android 版本支持
- **V2 (APK Signature Scheme v2)**: Android 7.0+ 支持，提供更好的安全性和性能
- **V3 (APK Signature Scheme v3)**: Android 9.0+ 支持，支持密钥轮换
- **V4 (APK Signature Scheme v4)**: Android 11.0+ 支持，支持流式安装

### 签名验证

构建时会输出签名配置信息：

```
=== Signing Configuration ===
Store file: /path/to/your/keystore.keystore
Store password: ***
Key alias: your_key_alias
Key password: ***
V1 signing: enabled
V2 signing: enabled
V3 signing: enabled
V4 signing: enabled
==============================
```

### 手动验证 APK 签名

```bash
# 验证 APK 签名版本
apksigner verify --verbose app-release.apk

# 检查具体签名信息
apksigner verify --print-certs app-release.apk
```

## 最佳实践

1. **安全性**: 永远不要将真实密钥库信息提交到代码仓库
2. **环境隔离**: 为不同环境（dev/staging/prod）使用不同的密钥库
3. **权限控制**: 限制 CI/CD 环境中密钥库文件的访问权限
4. **定期轮换**: 定期更换密钥库和密码
5. **备份**: 安全备份密钥库文件，确保可以重新构建历史版本

## 故障排除

### 常见问题

1. **构建失败：Keystore password not configured**
   - 检查环境变量是否正确设置
   - 确认 CI/CD 平台的 secret 配置
   - 验证命令行参数格式

2. **找不到密钥库文件**
   - 检查文件路径是否正确
   - 确认文件已上传到 CI/CD 环境
   - 使用绝对路径避免相对路径问题

3. **签名验证失败**
   - 确认密钥库密码和密钥密码正确
   - 检查密钥别名是否存在于密钥库中
   - 验证 APK 未被篡改

### 调试技巧

在构建命令前添加 `--info` 或 `--debug` 参数查看详细日志：

```bash
./gradlew assembleRelease --info
```

查看签名配置详情：

```bash
./gradlew signingReport
```