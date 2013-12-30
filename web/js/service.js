import mori from "mori";
import { request } from "/http";
import config from "/config";


var service = {};

service.getCollections = function() {
  return request({url: config.baseUrl + '/collections'})
  .then(function(response) {
    return mori.get(response, 'body');
  });
};


export default service;
