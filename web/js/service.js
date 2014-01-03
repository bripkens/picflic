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

service.getCollection = function(id) {
  return request({url: baseUrl + '/collections/' + id})
  .then(function(response) {
    return mori.get(response, 'body');
  });
};

service.getCollectionPreviewImage = function(collection) {
  var images = mori.get(collection, 'images');
  return mori.get(images, 0);
};


service.getImageUrl = function(collectionId, imageId) {
  return baseUrl + '/collections/' + collectionId + '/images/' + imageId;
};


service.getImageResolution = function(image, desiredWidth) {
  var resolutions = mori.get(image, 'resolutions');

  // try to find a larger version
  var larger = mori.some(function(resolution) {
    var width = mori.get(resolution, 'width');
    if (width >= desiredWidth) {
      return resolution;
    }
  }, resolutions);
  if (larger) return larger;

  // try to find a smaller version
  var smaller = mori.some(function(resolution) {
    var width = mori.get(resolution, 'width');
    if (width <= desiredWidth) {
      return resolution;
    }
  }, mori.reverse(resolutions));
  if (smaller) return smaller;

  // if all fails we return the original dimensions
  return mori.js_to_clj({
    width: mori.get(image, 'width'),
    height: mori.get(image, 'height')
  });
};


export default service;
