/**
 * @jsx React.DOM
 */

import mori from "mori";
import service from "/service";
import ImageGrid from "/components/image-grid";

 export default React.createClass({
  render: function() {
    var images = mori.map(function(collection) {
      var preview = service.getCollectionPreviewImage(collection);
      return mori.js_to_clj({
        label: mori.get(collection, 'name'),
        description: mori.get(collection, 'description'),
        src: service.getImageUrl(mori.get(collection, '_id'),
          mori.get(preview, '_id')),
        image: preview

      });
    }, this.props.collections);

    return (
      <div className='collection-grid'>
        <ImageGrid images={images}></ImageGrid>
      </div>
    );
  }
 });
