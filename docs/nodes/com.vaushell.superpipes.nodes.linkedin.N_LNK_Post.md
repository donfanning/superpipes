![SuperPipes](https://raw2.github.com/fabienvauchelles/superpipes/master/docs/images/logo_slogan238.png)


# Node: N_LNK_Post

Full class path : [`com.vaushell.superpipes.nodes.linkedin.N_LNK_Post`](../../superpipes/src/main/java/com/vaushell/superpipes/nodes/linkedin/N_LNK_Post.java)


## Goal

This node posts a status update to a LinkedIn profile.

__Warning!__ LinkedIn API disallows to delete a status.

To use the node, I create a LinkedIn application and get my credentials (key and secret).

See [How to create a LinkedIn application and get credentials](../tutorials/Create_LinkedIn_Application.md).


## Default parameters

* anti-burst: 2000 (2 seconds)
* delay: 0


## Standard parameters

Key | Description | Type | Required | Default value | Example value
 --- | --- | --- | --- | --- | --- 
timeout | Socket timeout in milliseconds. How long should I wait before the message is delivered?| long | no | 20000 | 20000
retry | How many times do I retry if the check fails ? | integer | no | 3 | 3
delayBetweenRetry | How long should I wait between 2 checks ? (in milliseconds) | long | no | 5000 | 5000
key | Application Key | string | yes | N/A | 435923492349
secret | Application Secret | string | yes | N/A | 012345679abcdef0123456

## Use example

![Example](https://raw2.github.com/fabienvauchelles/superpipes/master/docs/images/example_blog_to_linked.png)

I post a message from my blog to my LinkedIn profile:

```xml
<node id="linkedin-post" type="com.vaushell.superpipes.nodes.linkedin.N_LNK_Post">
    <params>
        <param name="key" value="435923492349" />
        <param name="secret" value="012345679abcdef0123456" />
    </params>
</node>
```