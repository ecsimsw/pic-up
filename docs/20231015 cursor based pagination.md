## Offset based paging to cursor based paging

### Offset based
```
Hibernate: 
    select
        product0_.id as id1_1_,
        product0_.name as name2_1_,
        product0_.price as price3_1_,
        product0_.quantity as quantity4_1_ 
    from
        product product0_ 
    order by
        product0_.id asc,
        product0_.id asc limit ?
```  

### Cursor based
```
Hibernate: 
    select
        product0_.id as id1_1_,
        product0_.name as name2_1_,
        product0_.price as price3_1_,
        product0_.quantity as quantity4_1_ 
    from
        product product0_ 
    where
        product0_.id>50 
    order by
        product0_.id asc,
        product0_.id asc limit ?
```

### Query test 1

db engine : mysql 8.0
data rows : 1_000_000
index : only id, full scan
iterations : 10

```
explain select * from product order by name asc limit 10 offset 0;          // filtered : 100%  

explain select * from product order by name asc limit 10 offset 900000;     // filtered : 100%

explain select * from product where name > 'a' order by name asc limit 10;  // filtered : 33% 

explain select * from product where name > 'y' order by name asc limit 10;  // filtered : 33%
```

```
select * from product order by name asc limit 10 offset 0;                   // about : 0.398 sec  

select * from product order by name asc limit 10 offset 900000;              // about : 2.504 sec 

select * from product where name > 'a' order by name asc limit 10;           // about : 0.407 sec 

select * from product where name > 'y' order by name asc limit 10;           // about : 0.154 sec
```

### Query test 2

db engine : mysql 8.0
data rows : 1_000_000
index : name, id, price, quantity (covering)
iterations : 10

```
ALTER TABLE `mymarket`.`product` ADD INDEX `index2` (`name` ASC, `id` ASC, `quantity` ASC, `price` ASC);  // 2.80 sec
```

```
explain select * from product order by name asc limit 10 offset 0;          // filtered : 100% / full index scan   

explain select * from product order by name asc limit 10 offset 900000;     // filtered : 100% / full index scan

explain select * from product where name > 'a' order by name asc limit 10;  // filtered : 100% / index range scan 

explain select * from product where name > 'y' order by name asc limit 10;  // filtered : 100% / index range scan
```

```
select * from product order by name asc limit 10 offset 0;                   // about : 0.009 sec  

select * from product order by name asc limit 10 offset 900000;              // about : 0.120 sec 

select * from product where name > 'a' order by name asc limit 10;           // about : 0.008 sec 

select * from product where name > 'y' order by name asc limit 10;           // about : 0.008 sec
```



### Result

<img width="713" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/734c4f33-9b26-4554-a8c6-c02f8dc03c02">


```
select * from product order by name asc limit 10 offset 0;

select * from product order by name asc limit 10 offset 900000;

select * from product where name > 'a' order by name asc limit 10; 

select * from product where name > 'y' order by name asc limit 10; 
```
