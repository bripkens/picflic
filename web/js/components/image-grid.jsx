/**
 * @jsx React.DOM
 */

 export default React.createClass({
  render: function() {
    var items = mori.map(function(image) {
      return (
        <li>
          <img src={mori.get(image, 'image')}/>
          <span>{mori.get(image, 'label')}</span>
        </li>
      );
    }, this.props.images);

    return (
      <ul className="image-grid">
        {mori.clj_to_js(items)}
      </ul>
    );
  }
 });
