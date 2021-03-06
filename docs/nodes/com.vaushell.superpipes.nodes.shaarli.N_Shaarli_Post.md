![SuperPipes](https://raw.githubusercontent.com/fabienvauchelles/superpipes/master/docs/images/logo_slogan238.png)


# Node: N_Shaarli_Post

Full class path : [`com.vaushell.superpipes.nodes.shaarli.N_Shaarli_Post`](../../superpipes/src/main/java/com/vaushell/superpipes/nodes/shaarli/N_Shaarli_Post.java)


## Goal

This node posts a link to Shaarli.

It uses [Shaari JAVA API](https://github.com/fabienvauchelles/shaarli-java-api).


## Default parameters

* anti-burst: 2000 (2 seconds)
* delay: 0


## Standard parameters

Key | Description | Type | Required | Default value | Example value
 --- | --- | --- | --- | --- | ---
retry | How many times do I retry if the check fails ? | integer | no | 10 | 10
wait-time | How long should I wait between 2 checks ? (in milliseconds) | long | no | 5000 | 5000
wait-time-multiplier | How multiple I `wait-time` each time ? | double | no | 2.0 | 2.0
jitter-range | Add or substract randomly time to `wait-time` (between 0 and `jitter-range`) | int | no | 500 | 500
max-duration | How long shoud I retry ? (in milliseconds, 0=disabled) | long | no | 0 | 10000
url | URI of the RSS feed | string | yes | N/A | http://feeds.feedburner.com/lesliensducode
login | Login | string | yes | N/A | monlogin
password | Password | string | yes | N/A | 1234

## Template parameters

If the site template changes, I specify to the API how to find essential information.

Key | Description | Type | Required | Default value | Example value
 --- | --- | --- | --- | --- | ---
key | See the documentation Shaari JAVA API | string | yes | N/A | links-count
csspath | See the documentation Shaari JAVA API | string | yes | (empty string) | form[name=searchform] input[class=medium]
attribut | See the documentation Shaari JAVA API | string | yes | (empty string) | placeholder
regex | See the documentation Shaari JAVA API | regular expression | yes | (empty string) | \\d+


## Use example

![Example](https://raw.githubusercontent.com/fabienvauchelles/superpipes/master/docs/images/example_migrate_shaarli.png)

I post links of [Sebsauvage's Shaarli](http://sebsauvage.net/links) to my Shaarli 'http://feeds.feedburner.com/lesliensducode':

```xml
<node id="shaarli-write" type="com.vaushell.superpipes.nodes.rss.N_RSS">
    <params>
        <param name="url" value="http://feeds.feedburner.com/lesliensducode" />
        <param name="login" value="monlogin" />
        <param name="password" value="1234" />
    </params>
</node>
```
