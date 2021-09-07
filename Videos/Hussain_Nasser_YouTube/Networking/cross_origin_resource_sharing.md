Video: 15:00

# Cross-Origin Resource sharing

1. A web resource has a certain policies to be shared with certain domains
2. If you open http://www.example.com in browser and try to execute an AJAX call to fetch a locally running index.html at http://localhost/index.html, it gives a CORS error because you are trying to access a different domain than example.com. 
3. To fix this, in the local Web server's config, set the Access-Control-Allow-Origin header to http://www.example.com
4. To allow everybody to programmatically access to http://localhost/index.html, change the value to '*'
5. Preflight request: A preflight request asks for the server’s permission to send the request. The preflight isn’t the request itself. Instead, it contains metadata about it, such as which HTTP method is used and if the client added additional request headers. The server inspects this metadata to decide whether the browser is allowed to send the request
6. To have a preflight request, we need to implement the OPTIONS method and pass Access-Control-Allow-Header, Access-Control-Allow-Origin headers. 
