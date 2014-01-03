/**
 * @jsx React.DOM
 */

import crossroads from "crossroads";
import mori from "mori";

import CollectionGrid from "/components/collection-grid";
import CollectionDetail from "/components/collection-detail";
import service from "/service";

export default React.createClass({
  getInitialState: function() {
    return mori.hash_map('collections', mori.vector(), 'content', null);
  },

  componentDidMount: function() {
    var setContent = function(content) {
      this.replaceState(mori.assoc(this.state, 'content', content));
    }.bind(this);

    crossroads.addRoute('', function() {
      service.getCollections()
      .then(function(collections) {
        setContent(CollectionGrid({
          collections: collections
        }));
      })
      .done();
    });

    crossroads.addRoute('collections/{collectionId}', function(collectionId) {
      service.getCollection(collectionId)
      .then(function(collection) {
        setContent(CollectionDetail({
          collection: collection
        }));
      })
      .done();
    }.bind(this));

    function parseHash(newHash, oldHash){
      crossroads.parse(newHash);
    }
    hasher.initialized.add(parseHash);
    hasher.changed.add(parseHash);
    hasher.init();
  },

  render: function() {
    var content = mori.get(this.state, 'content');

    return (
      <div>
        {content ?
          <main>
            {content}
          </main>
          :
          null}
      </div>
    );
  }
});
