define(['$','sl/sl','app'],function (require,exports,module) {
    var $=require('$'),
        sl=require('sl/sl'),
        app=require('app'),
        util=require('util');

    module.exports=sl.Activity.extend({
        template: 'views/photosave.html',
        events: {
            'tap': function (e) {
                if($(e.target).hasClass('view')) {
                    this.back();
                }
            },
            'tap .js_save': function () {
                var value=this.$('input').val();
                if(!value) {
                    sl.tip('请填写图片描述');
                    return;
                }

                this.setResult("photoSave",value);
                this.back();
            }
        },
        className: 'transparent1',
        animationName: 'search',
        onCreate: function () {
            var that=this;

        },
        onStart: function () {
        },
        onResume: function () {
        },
        onShow: function () {
            this.$('input').focus();
        },
        onDestory: function () {
        }
    });;
});