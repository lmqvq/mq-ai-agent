# 🤝 贡献指南 / Contributing Guide

感谢您对 MQ AI Agent 项目的关注！我们欢迎所有形式的贡献，包括但不限于代码贡献、文档改进、问题反馈和功能建议。

Thank you for your interest in the MQ AI Agent project! We welcome all forms of contributions, including but not limited to code contributions, documentation improvements, issue reports, and feature suggestions.

## 📋 目录 / Table of Contents

- [如何贡献 / How to Contribute](#如何贡献--how-to-contribute)
- [开发环境设置 / Development Environment Setup](#开发环境设置--development-environment-setup)
- [代码规范 / Code Standards](#代码规范--code-standards)
- [提交规范 / Commit Standards](#提交规范--commit-standards)
- [Pull Request 流程 / Pull Request Process](#pull-request-流程--pull-request-process)
- [问题报告 / Issue Reporting](#问题报告--issue-reporting)
- [功能建议 / Feature Requests](#功能建议--feature-requests)

## 🚀 如何贡献 / How to Contribute

### 1. Fork 项目 / Fork the Project

```bash
# 克隆你的 fork / Clone your fork
git clone https://github.com/lmqvq/mq-ai-agent.git
cd mq-ai-agent

# 添加上游仓库 / Add upstream repository
git remote add upstream https://github.com/original-owner/mq-ai-agent.git
```

### 2. 创建分支 / Create a Branch

```bash
# 创建并切换到新分支 / Create and switch to new branch
git checkout -b feature/your-feature-name

# 或者修复 bug / Or for bug fixes
git checkout -b fix/your-bug-fix
```

### 3. 进行更改 / Make Changes

- 确保代码符合项目的编码规范
- 添加必要的测试用例
- 更新相关文档

### 4. 提交更改 / Commit Changes

```bash
# 添加更改 / Add changes
git add .

# 提交更改 / Commit changes
git commit -m "feat: add new feature description"
```

### 5. 推送分支 / Push Branch

```bash
git push origin feature/your-feature-name
```

### 6. 创建 Pull Request / Create Pull Request

在 GitHub 上创建 Pull Request，详细描述你的更改。

## 🛠️ 开发环境设置 / Development Environment Setup

### 必需软件 / Required Software

- **Java**: 21+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Git**: 2.0+
- **IDE**: IntelliJ IDEA 或 Eclipse

### 环境配置 / Environment Configuration

1. **克隆项目 / Clone Project**
```bash
git clone https://github.com/lmqvq/mq-ai-agent.git
cd mq-ai-agent
```

2. **配置数据库 / Configure Database**
```bash
# 创建数据库 / Create database
mysql -u root -p
CREATE DATABASE mq_ai_agent;
USE mq_ai_agent;
SOURCE sql/create_table.sql;
```

3. **配置应用 / Configure Application**
```yaml
# src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mq_ai_agent
    username: your_username
    password: your_password
  
  ai:
    dashscope:
      api-key: your_test_api_key
```

4. **运行项目 / Run Project**
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

## 📝 代码规范 / Code Standards

### Java 代码规范 / Java Code Standards

我们遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)：

- **缩进**: 使用 4 个空格，不使用 Tab
- **行长度**: 最大 120 字符
- **命名规范**: 
  - 类名使用 PascalCase
  - 方法名和变量名使用 camelCase
  - 常量使用 UPPER_SNAKE_CASE

### 注释规范 / Comment Standards

```java
/**
 * 类的简要描述
 * 
 * @author 作者名
 * @create 创建日期
 * @version 版本号
 */
public class ExampleClass {
    
    /**
     * 方法的简要描述
     *
     * @param param1 参数1描述
     * @param param2 参数2描述
     * @return 返回值描述
     * @throws Exception 异常描述
     */
    public String exampleMethod(String param1, int param2) throws Exception {
        // 实现逻辑
        return "result";
    }
}
```

### 测试规范 / Testing Standards

- 每个公共方法都应该有对应的单元测试
- 测试覆盖率应该达到 80% 以上
- 使用 JUnit 5 和 Mockito 进行测试

```java
@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {
    
    @Mock
    private ExampleRepository exampleRepository;
    
    @InjectMocks
    private ExampleService exampleService;
    
    @Test
    @DisplayName("测试方法描述")
    void testExampleMethod() {
        // Given
        String input = "test input";
        String expected = "expected output";
        
        // When
        String actual = exampleService.exampleMethod(input);
        
        // Then
        assertEquals(expected, actual);
    }
}
```

## 📋 提交规范 / Commit Standards

我们使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

### 提交消息格式 / Commit Message Format

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### 提交类型 / Commit Types

- `feat`: 新功能 / New feature
- `fix`: 修复 bug / Bug fix
- `docs`: 文档更新 / Documentation update
- `style`: 代码格式调整 / Code style changes
- `refactor`: 代码重构 / Code refactoring
- `test`: 测试相关 / Test related
- `chore`: 构建过程或辅助工具的变动 / Build process or auxiliary tool changes

### 示例 / Examples

```bash
# 新功能 / New feature
git commit -m "feat: add user authentication system"

# 修复 bug / Bug fix
git commit -m "fix: resolve memory leak in chat service"

# 文档更新 / Documentation update
git commit -m "docs: update API documentation"

# 重构 / Refactoring
git commit -m "refactor: optimize database query performance"
```

## 🔄 Pull Request 流程 / Pull Request Process

### PR 标题格式 / PR Title Format

```
[Type] Brief description of changes
```

例如 / Example:
- `[Feature] Add conversation history management`
- `[Fix] Resolve chatId prefix duplication issue`
- `[Docs] Update README with deployment guide`

### PR 描述模板 / PR Description Template

```markdown
## 📝 变更描述 / Change Description

简要描述本次 PR 的主要变更内容。

## 🎯 变更类型 / Change Type

- [ ] 新功能 / New feature
- [ ] Bug 修复 / Bug fix
- [ ] 文档更新 / Documentation update
- [ ] 代码重构 / Code refactoring
- [ ] 性能优化 / Performance improvement
- [ ] 其他 / Other

## 🧪 测试 / Testing

- [ ] 已添加单元测试 / Unit tests added
- [ ] 已添加集成测试 / Integration tests added
- [ ] 手动测试通过 / Manual testing passed
- [ ] 所有现有测试通过 / All existing tests pass

## 📋 检查清单 / Checklist

- [ ] 代码符合项目规范 / Code follows project standards
- [ ] 已更新相关文档 / Documentation updated
- [ ] 已添加必要的测试 / Necessary tests added
- [ ] 提交消息符合规范 / Commit messages follow standards

## 🔗 相关问题 / Related Issues

Closes #issue_number
```

### 代码审查 / Code Review

所有 PR 都需要经过代码审查：

1. **自动检查**: CI/CD 流水线会自动运行测试和代码质量检查
2. **人工审查**: 至少需要一名维护者的审查和批准
3. **反馈处理**: 根据审查意见及时修改代码

## 🐛 问题报告 / Issue Reporting

### Bug 报告模板 / Bug Report Template

```markdown
## 🐛 Bug 描述 / Bug Description

简要描述遇到的问题。

## 🔄 复现步骤 / Steps to Reproduce

1. 执行操作 A
2. 执行操作 B
3. 观察到错误

## 🎯 期望行为 / Expected Behavior

描述你期望发生的行为。

## 📱 环境信息 / Environment

- OS: [e.g. Windows 10, macOS 12.0, Ubuntu 20.04]
- Java Version: [e.g. 21]
- Spring Boot Version: [e.g. 3.4.6]
- Browser: [e.g. Chrome 91.0]

## 📎 附加信息 / Additional Information

添加任何其他有助于解决问题的信息，如截图、日志等。
```

## 💡 功能建议 / Feature Requests

### 功能请求模板 / Feature Request Template

```markdown
## 🚀 功能描述 / Feature Description

简要描述建议的新功能。

## 🎯 问题背景 / Problem Background

描述这个功能要解决的问题或改进的场景。

## 💡 解决方案 / Proposed Solution

描述你建议的解决方案。

## 🔄 替代方案 / Alternative Solutions

描述你考虑过的其他解决方案。

## 📋 附加信息 / Additional Information

添加任何其他相关信息或上下文。
```

## 🏆 贡献者认可 / Contributor Recognition

我们会在以下地方认可贡献者：

- README.md 中的贡献者列表
- 发布说明中的特别感谢
- 项目官网的贡献者页面

## 📞 联系方式 / Contact

如果你有任何问题或需要帮助，可以通过以下方式联系我们：

- **GitHub Issues**: [项目问题页面](https://github.com/lmqvq/mq-ai-agent/issues)
- **Email**: lmqicu@qq.com
- **讨论区**: [GitHub Discussions](https://github.com/lmqvq/mq-ai-agent/discussions)

---

再次感谢您的贡献！🎉

Thank you again for your contribution! 🎉
