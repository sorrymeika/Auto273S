define(['$','sl/sl','app','sl/widget/loading','sl/widget/imglazyload','sl/widget/dialog'],function (require,exports,module) {
    var $=require('$'),
        sl=require('sl/sl'),
        app=require('app'),
        ImgLazyload=require('sl/widget/imglazyload'),
        Dialog=require('sl/widget/dialog'),
        Loading=require('sl/widget/loading');

    module.exports=sl.Activity.extend({
        template: 'views/photolist.html',
        events: {
            'tap .js_list li': 'check',
            'tap .js_delete': 'del',
            'tap .js_back': 'back'
        },
        onCreate: function () {
            var that=this;

            that.$list=that.$('.js_list');

            that.imgLazyload=new ImgLazyload();


            that.loading=new Loading(that.$list);


            that.load();


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
            $target.find('i').toggleClass('checked');
        },

        load: function () {
            var that=this;

            var userinfo=JSON.parse(localStorage.getItem('USERINFO'));
            that.$list.html("");
            that.loading.load({
                url: '/json/getupload?transferid='+that.route.data.id,
                data: {
                    auth: userinfo.Auth,
                    account: userinfo.AccountName
                },
                success: function (res) {
                    this.hideLoading();
                    that.isDataLoad=true;
                    that.$list.html(that.tmpl('list',res));
                    that.imgLazyload.add(that.$('.js_lazy[data-url]').removeClass('js_lazy'));
                },
                refresh: function (res) {
                    that.$list.append(that.tmpl('list',res));
                },
                error: function (res) {
                    this.hideLoading();
                    sl.tip(res.msg);
                }
            });
        },

        del: function () {
            var that=this,
                items=that.$list.find('i.checked'),
                ids=[],
                userinfo=JSON.parse(localStorage.getItem('USERINFO'));

            sl.confirm("确认删除？",function () {
                items.each(function () {
                    ids.push($(this).data('id'));
                });

                that.loading.load({
                    url: '/json/getupload?action=delete',
                    data: {
                        auth: userinfo.Auth,
                        account: userinfo.AccountName,
                        ids: ids.join(',')
                    },
                    checkData: false,
                    success: function (res) {
                        this.hideLoading();
                        sl.tip('删除成功！');

                        items.each(function () {
                            $(this).parent().remove();
                        });
                    },
                    error: function (res) {
                        this.hideLoading();
                        sl.tip(res.msg);
                    }
                });
            });

        }

    });
});
