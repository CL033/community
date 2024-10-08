$(function (){
    $("#TopBtn").click(setTop);
    $("#WonderfulBtn").click(setWonderful);
    $("#DeleteBtn").click(setDelete);
})
function like(btn,entityType,entityId,entityUserId,postId){
    $.post(
        CONTEX_PATH+"/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data){
            data = $.parseJSON(data);
            if(data.code==0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else {
                alert(data.msg)
            }
        }
    )
}
//置顶
function setTop() {
    $.post(
        CONTEX_PATH+"/discuss/top",
        {"id":$("#postId").val()},
        function (data){
            data=$.parseJSON(data);
            if(data.code==0){
                $("#TopBtn").attr("disabled","disabled")
            }else {
                alert(data.msg)
            }
        }
    )
}

//加精
function setWonderful() {
    $.post(
        CONTEX_PATH+"/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data){
            data=$.parseJSON(data);
            if(data.code==0){
                $("#WonderfulBtn").attr("disabled","disabled")
            }else {
                alert(data.msg)
            }
        }
    )
}

//删除
function setDelete() {
    $.post(
        CONTEX_PATH+"/discuss/delete",
        {"id":$("#postId").val()},
        function (data){
            data=$.parseJSON(data);
            if(data.code==0){
                location.href=CONTEX_PATH+"/index"
            }else {
                alert(data.msg)
            }
        }
    )
}