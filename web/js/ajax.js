function toResponse(request) {
  console.log(request.getAllResponseHeaders())
  return {
    status: request.status,
    response: request.response
  };
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

export default request;
