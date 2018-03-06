# 使い方

## Entityの書き方
JPAのアノテーションの一部をそのまま使用する。

例:
```java
@Getter
@Setter
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue
    private Long id;
 
    private String name;
 
    @Column(name = "value")
    private String nickname;
    
    @Transient
    private String password;
}
```

エンティティクラスには `@Table`でテーブル名を指定する。
primary keyなカラムには `@Id` を指定。
auto incrementなら `@GeneratedValue` を指定。

カラム名を指定するなら`@Column`でnameを指定。
プロパティ名とカラム名が同一なら`@Column`を付けなくてよい。

`@Transient`アノテーションを付けたプロパティ/フィールドの値はテーブルに保存されない。

### 複合主キー
複数のカラムに `@Id` を付ける。複合主キークラスの定義は不要。
自動で定義されるupdateを呼ぶ際にのみ複合主キーが考慮される。

## クエリの書き方
基本的にはSpring Data JPAとだいたい同じように書ける。
ただし、アノテーションは独自のものを使う。

`@Query` でクエリを指定。
更新するクエリは `@Modifying` を付ける。

例:
```java
@Repository
public interface UserRepository extends JdbcTemplateRepository<User> {
    @Query("SELECT * FROM user WHERE name = :name")
    User getByName(@Param("name") String name);
 
    @Query("SELECT * FROM users WHERE name IN (:names)")
    List<User> getByNames(@Param("names") List<String> names);
 
    @Modifying
    @Query("UPDATE user SET nickname = :nickname WHERE id = :id")
    void updateNickname(@Param("id") Long id, @Param("nickname") String nickname);
}
```

### update と insert
entityを指定してのupdate,insert,insertAndReturnKeyのみ自動で定義される。
insertした際にauto incrementなkeyを取得したい時は、insertAndReturnKeyを呼ぶことで返り値として返ってくる。
その他は自分でクエリの定義が必要。

#### コールバック
EntityCallback インターフェースを実装することで、insert/updateの前後でコールバックメソッドを呼んでくれる。

JPAでの、`@PrePersist`, `@PostPersist`, `@PreUpdate`, `@PostUpdate` アノテーションの代替。

### Single-column query
countなどカラムが1つのクエリを書く時は、@SingleColumnアノテーションを付ける。

例:
```java
@SingleColumn
@Query(value = "SELECT count(*) FROM user")
long count();
```

## Mapperのカスタマイズ
デフォルトではResutSetからEntityへのマッピングをリフレクションで行っている。

以下の場合はカスタマイズが必要。

* 高速化のため
* リレーションを扱いたい時

```java
@Mapper(YourCustomizedMapper.class)
@Query("SELECT * FROM 〜")
public SomeEntity getWithMapper();
```

上記のように、`@Mapper` アノテーションを、クエリメソッド、リポジトリインターフェース、エンティティクラス、
のいずれかに付けることでMapperを指定する。

Mapperクラスは`EntityMapper` インターフェースを実装しなければならない。
通常は、`AbstractEntityMapper`を継承して、`setValue`メソッドだけオーバーライドすれば良い。
`setValue`メソッドには、Entityのインスタンス、カラム名、値が渡ってくるので、値をセットする。

## 設定方法
### Repositoryを有効化する
Configuration Classに アノテーション `@EnableJdbcTemplateRepositories` を付けることで有効化する。
basePackagesプロパティにスキャン対象のパッケージを記述する。

例:
```java
@EnableJdbcTemplateRepositories(basePackages = "your.repository.package")
@Configuration
public class ApplicationConfig {
    
} 
```

### NamedParameterJdbcTemplate のインスタンスをBeanとして登録する
データベースへのアクセスは、`NamedParameterJdbcTemplate` クラスを使用して行う。
`NamedParameterJdbcTemplate` クラスのインスタンスをbeanとして登録することで、データベースにアクセス出来る。

例:
```java
@Bean
public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
    DateSource dataSource = /* DateSourceの初期化 */;
    return new NamedParameterJdbcTemplate(dataSource);
}
```
