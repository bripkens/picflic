/**
 * @jsx React.DOM
 */

import mori from "mori";

import CollectionList from "/components/collection-list";
import CollectionGrid from "/components/collection-grid";
import service from "/service"

export default React.createClass({
  getInitialState: function() {
    return mori.hash_map('collections', mori.vector());
  },

  componentWillMount: function() {
    service.getCollections()
    .then(function(collections) {
      var newState = mori.assoc(self.state, 'collections', collections);
      this.replaceState(newState);
    }.bind(this))
    .done();
  },

  render: function() {
    return (
      <div>
        <nav>
          <CollectionList collections={mori.get(this.state, 'collections')}>
          </CollectionList>
        </nav>
        <main>
          <CollectionGrid collections={mori.get(this.state, 'collections')}>
          </CollectionGrid>
        </main>
      </div>
    );
  }
});
