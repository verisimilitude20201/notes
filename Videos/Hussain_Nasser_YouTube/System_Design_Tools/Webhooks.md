Video: https://www.youtube.com/watch?v=-4Lid7tBr6Y&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=23(05:00)

# Webhooks

1. Custom callback URLs that an application can use to communicate with other application
2. For example: When someone uploads a video on Youtube, Youtube call Discord API to post it on a discord channel. Github & Slack are other applications that use Webhooks.
3. URLs that one application can provide which other applications can call on certain actions. For example: app1 decides to create have a URL http://app1/123 as Webhook. This URL gets sent to app2. app2 calls http://app1/123 on occurence of certain events. app1 knows app2 can call this URL