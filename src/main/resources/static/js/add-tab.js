$(document).ready(function(){
    var next = 0;
    $("#add-member").click(function(e){
        e.preventDefault();
        var addto = "#members-area";
        var newIn = '<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4" id="remove-me-'+ next +'"><label class="sr-only" for="inlineMember' +  next + '">Member</label><div class="input-group mb-3"><div class="input-group-prepend"><div class="input-group-text">@</div></div><input type="text" class="form-control" required id="inlineMember' +  next + '" name="members['+ next + ']"placeholder="Member"><div class="input-group-append"><button id="remove' + next + '" class="remove-member btn btn-danger" type="button">Remove</button></div></div></div>';
        var newInput = $(newIn);
        next = next + 1;
        $(addto).append(newInput);
        $("#field" + next).attr('data-source',$(addto).attr('data-source'));
        $("#count").val(next);
        $('.remove-member').click(function(e){
            e.preventDefault();
            var fieldNum = this.id.substring("remove".length);
            var fieldID = "#remove-me-" + fieldNum;
            $(this).remove();
            $(fieldID).remove();
        });
    });
});