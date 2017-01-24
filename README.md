# Spring Data Jdbc Template

## How to Configure
### Enable Repositories
Annotate your configuration class with `@EnableJdbcTemplateRepositories`.

Example:
```java
@EnableJdbcTemplateRepositories(basePackages = "your.repository.package")
@Configuration
public class ApplicationConfig {
    
} 
```

### Register NamedParameterJdbcOperations Bean
This library uses NamedParameterJdbcTemplate.
You should register its bean.

Example:
```java
@Bean
public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
  return new NamedParameterJdbcTemplate();
}
``` 
