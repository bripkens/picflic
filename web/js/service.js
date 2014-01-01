import mori from "mori";
import { request } from "/http";
import config from "/config";
import util from "/util";

var baseUrl = mori.get(config, 'baseUrl');

var service = {};

service.getCollections = function() {
  return request({url: baseUrl + '/collections'})
  .then(function(response) {
    return mori.get(response, 'body');
  });
};

service.getCollectionPreviewImage = function(collection) {
  var images = mori.get(collection, 'images');
  var image = mori.get(images, 0);
  return service.getImageUrl(mori.get(collection, '_id'),
    mori.get(image, '_id'));
};


service.getImageUrl = function(collectionId, imageId) {
  return baseUrl + '/collections/' + collectionId + '/images/' + imageId;
};


export default service;
