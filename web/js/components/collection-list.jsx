/**
 * @jsx React.DOM
 */

import mori from "mori";

export default React.createClass({
  render: function() {
    var collectionNodes = mori.map(function(collection) {
      return <li>{mori.get(collection, 'name')}</li>
    }, this.props.collections);

    return (
      <ul>
        {mori.clj_to_js(collectionNodes)}
      </ul>
    );
  }
});
