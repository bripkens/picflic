/**
 * @jsx React.DOM
 */

import mori from "mori";

import service from "/service";
import ImageGrid from "/components/image-grid";

 export default React.createClass({
  render: function() {
    var collection = this.props.collection;

    var images = mori.map(function(image) {
      var url = service.getImageUrl(mori.get(collection, '_id'),
        mori.get(image, '_id'))
      return mori.js_to_clj({
        label: null,
        description: null,
        src: url,
        image: image,
        link: url
      });
    }, mori.get(collection, 'images'));

    return (
      <div>
        <h1>{mori.get(collection, 'name')}</h1>
        <ImageGrid images={images}></ImageGrid>
      </div>
    );
  }
 });
