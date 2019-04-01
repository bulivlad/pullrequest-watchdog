/**
 * jQuery.fn.sorter
 * --------------
 * @param Function comparator:
 *   Exactly the same behaviour as [1,2,3].sort(comparator)
 *
 * @param Function getSortable
 *   A function that should return the element that is
 *   to be sorted. The comparator will run on the
 *   current collection, but you may want the actual
 *   resulting sort to occur on a parent or another
 *   associated element.
 *
 *   E.g. $('td').sortElements(comparator, function(){
 *      return this.parentNode;
 *   })
 *
 *   The <td>'s parent (<tr>) will be sorted instead
 *   of the <td> itself.
 *   https://j11y.io/javascript/sorting-elements-with-jquery/
 */

 jQuery.fn.sorter = (function(){
    var sort = [].sort;
    return function(comparator, getSortable) {
        getSortable = getSortable || function(){return this;};
        var last = null;
        return sort.call(this, comparator).each(function(i){
            // at this point the array is sorted, so we can just detach each one from wherever it is, and add it after the last
            var node = getSortable && typeof(getSortable) === "function" ? getSortable.call(this) : $(this);
            var parent = node.parentNode;
            if (last) {
                last.after(node);
            } else {
                parent.prepend(node);
            }
            last = node;
        });
    };
})();
