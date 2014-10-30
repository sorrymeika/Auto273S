define(['$','sl/sl','app','sl/widget/loading','sl/widget/imglazyload'],function (require,exports,module) {
    var $=require('$'),
        sl=require('sl/sl'),
        app=require('app'),
        ImgLazyload=require('sl/widget/imglazyload'),
        Loading=require('sl/widget/loading');

    module.exports=sl.Activity.extend({
        template: 'views/photo.html',
        events: {
            'tap .js_list li.item': 'check',
            'tap .js_save': 'save',
            'tap .js_back': 'back'
        },
        onCreate: function () {
            var that=this;

            that.$list=that.$('.js_list');

            that.imgLazyload=new ImgLazyload(that.$('.js_lazy[data-url]').removeClass('js_lazy'));

            app.queryThumbnailList(function (res) {
                that.$list.append(that.tmpl('list',{
                    data: res
                }));
            });

        },
        onStart: function () {
        },
        onResume: function () {
        },
        onShow: function () {
            if(!localStorage.getItem('USERINFO')) {
                this.back('/');
            }
        },
        onDestory: function () {
            this.loading&&this.loading.destory();
            this.imgLazyload&&this.imgLazyload.destory();
        },

        check: function (e) {
            var $target=$(e.currentTarget);
            $target.toggleClass('check');

            this.$(".js_save").html('保存('+this.$list.find('li.check').length+')');
        },

        save: function () {
        }

    });
});
