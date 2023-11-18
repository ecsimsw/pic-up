## H2 version issue

Error : Syntax error in SQL statement "SELECT [*]VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME = 'MODE'";    
Sol   : Downgrade h2 version latest to 1.4.199    

```
INFORMATION_SCHEMA in H2 2.x.y is not compatible with INFORMATION_SCHEMA from H2 1.x.y.

You need to use

SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE SETTING_NAME = 'MODE'
with new versions of H2.

If this query was executed by Flyway, you need to upgrade it to 8.2.2 or any newer version, older versions don't support recent versions of H2.

You also need to check versions of other libraries, for example, if you use Hibernate ORM, you need to upgrade it to 5.6.5.Final (or later version). Older versions also don't support H2 2.x.y.

Please also note that H2 2.0.202 is an old release with many new features and also many bugs and regressions, it will be better to use H2 2.1.210, it contains various important fixes.
```