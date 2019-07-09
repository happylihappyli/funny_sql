# funny_sql
 process csv file as SQL DB
把文本文件当做SQL数据库来用
比如：

java -jar funny_sql.jar /root/123.txt 500000 "SELECT c7,c8,c9,c10,c11,c13,c14,c15,c16,c17,c18,
map(c19,'/root/abc.map'),
map(c20,'/root/abc.map'),
c21,c22,c23,
map(c24,'/root/abc.map'),
map(c25,'/root/abc.map'),1-c6
 From T " ","
