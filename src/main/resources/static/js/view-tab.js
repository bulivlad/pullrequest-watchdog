$(document).ready(function(){
    $("#search").on("keyup", function() {
        var value = $(this).val().toLowerCase();
        $("#team-table tbody tr").filter(function() {
            $(this).toggle($(this).text().toLowerCase().indexOf(value) > -1)
        });
    });
    var table = $('#team-table');
    $('th.sortable').wrapInner('<span title="sort this column"/>').each(function(){
        var th = $(this), thIndex = th.index(), inverse = false;
        th.click(function(){
            table.find('td').filter(function(){
                return $(this).index() === thIndex;
            }).sorter(function(a, b){
                if( $.text([a]) == $.text([b]) ) {
                    return 0;
                }
                return $.text([a]) > $.text([b]) ? inverse ? -1 : 1 : inverse ? 1 : -1;
            }, function(){
                // parentNode is the element we want to move
                return this.parentNode;
            });
            inverse = !inverse;
        });
    });
});
