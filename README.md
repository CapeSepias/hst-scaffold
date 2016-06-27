[![Build Status](https://travis-ci.org/jbloemendal/hst-scaffold.svg?branch=master)](https://travis-ci.org/jbloemendal/hst-scaffold)
![HST-Scaffold](https://raw.githubusercontent.com/jbloemendal/hst-scaffold/master/logo.png)
HST-Scaffold
============

Scaffold your projects Hippo Site Toolkit configuration from text file.

Build:
```
mvn clean verify
mv target/hsd-jar-with-dependencies.jar hsd.jar
```

Example Scaffold (scaffold.hst):
```
#HST scaffold example

#URL              CONTENTPATH                   COMPONENTS
/                 /home                         home(header,main(banner, text),footer) # home_page
/simple           /simple                       simple                                 # simple_page
/contact          /contact                      text(header,main(banner, text),footer) # text_page
/news/:date/:id   /news/date:String/id:String   news(header,main(banner, news),footer) # news_page
/text/*path       /text/path:String             text(header,main(banner, text),footer) # text_page
```

Usage:
```
java -jar hsd.jar [options] [args]

Options
-h     --help               Show help
-b     --build              Build configuration from scaffold.
-c     --configuration      Custom configuration file.
-u     --update             Update configuration from scaffold
-s     --scaffold           Build scaffold from existing project configuration (reverse).
-r     --rollback           Rollback Build / Update / Scaffold.

```