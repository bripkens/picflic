import mori from "mori";


function parseBody(contentType, body) {
  if (contentType.indexOf('application/json') === 0 ||
      contentType.match(/^application\/[^+]+\+json.*/)) {
    var result =  mori.js_to_clj(JSON.parse(body));
    return result;
  }
  return body;
}


function extractHeaders(headerString) {
  return headerString.split('\n')
  .filter(mori.identity)
  .map(function(header) {
    return header.split(':')
      .map(function(s) {
        return s.trim();
      });
  })
  .reduce(function(map, header) {
    return mori.assoc(map, header[0], header[1]);
  }, mori.hash_map());
}


function toResponse(request) {
  var headers = extractHeaders(request.getAllResponseHeaders());
  var response = mori.hash_map('status', request.status,
    'headers', headers,
    'body', parseBody(mori.get(headers, 'Content-Type'), request.response));
  return response;
}


function request(opts) {
  return new Promise(function(resolve, reject) {
    var request = new XMLHttpRequest();

    request.onreadystatechange = function() {
      if (request.readyState !== 4) return;

      var status = request.status;
      var response = toResponse(request);
      if (199 < status && status < 300) {
        resolve(response);
      } else {
        reject(response);
      }
    };

    request.open(opts.method || 'GET', opts.url);
    request.send();
  });
}

export { request };
