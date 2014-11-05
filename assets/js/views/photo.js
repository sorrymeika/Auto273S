define(['$','sl/sl','app','sl/widget/loading','sl/widget/imglazyload','sl/widget/dialog'],function(require,exports,module) {
    var $=require('$'),
        sl=require('sl/sl'),
        app=require('app'),
        ImgLazyload=require('sl/widget/imglazyload'),
        Dialog=require('sl/widget/dialog'),
        Loading=require('sl/widget/loading');

    module.exports=sl.Activity.extend({
        template: 'views/photo.html',
        events: {
            'tap .js_list li.item': 'check',
            'tap .js_save': 'save',
            'tap .js_back': 'back',
            'tap .js_take': 'takePhoto',
            'tap .js_select': 'pickImage',
            'tap .js_photolist': function() {
                this.forward('/photolist/'+this.route.data.id+'.html');
            }
        },
        onCreate: function() {
            var that=this;

            that.$list=that.$('.js_list');

            that.listenResult("photoSave",function(e,data) {
                var checked=that.$list.find('li.check'),
                    length=checked.length,
                    i=0,
                    photo,
                    error=0;

                if(length==0) {
                    sl.tip('请至少选择一张图片');
                    return;
                }

                !that.loading&&(that.loading=new Loading(that.$el));
                that.loading.showLoading();

                var userinfo=JSON.parse(localStorage.getItem('USERINFO'));


                var post=function() {
                    if(i<length) {
                        photo=checked.eq(i).data('path');

                        app.post('/json/upload',{
                            TransferID: that.route.data.id,
                            Description: data,
                            auth: userinfo.Auth,
                            account: userinfo.AccountName
                        },{
                            Photo: photo
                        },function(res) {
                            if(res&&res.success) {
                            } else {
                                error++
                            }

                            post();
                        });
                    } else {
                        sl.tip(error==0?"全部图片上传成功！":((length-error)+"张成功,"+error+"张失败！"));
                        that.loading.hideLoading();
                    }
                    i++;
                };

                post();
            });

            that.imgLazyload=new ImgLazyload(that.$('.js_lazy[data-url]').removeClass('js_lazy'));

            app.queryThumbnailList(function(res) {
                //that.$list.append(that.tmpl('list',{ data: res }));
            });

        },
        onStart: function() {
        },
        onResume: function() {
        },
        onShow: function() {
            if(!localStorage.getItem('USERINFO')) {
                this.back('/');
            }
        },
        onDestory: function() {
            this.loading&&this.loading.destory();
            this.imgLazyload&&this.imgLazyload.destory();
        },

        check: function(e) {
            var $target=$(e.currentTarget);
            $target.toggleClass('check');

            this.$(".js_save").html('保存('+this.$list.find('li.check').length+')');
        },

        save: function() {
            if(this.$list.find('li.check').length==0) {
                sl.tip('请至少选择一张图片');
                return;
            }
            this.forward('/photosave.html');
        },

        _appendImage: function(res) {
            var that=this;

            that.$list.append('<li class="item check" data-path="'+res.path+'" hl><img class="js_lazy" src="'+res.src+'" /></li>');
            this.$(".js_save").html('保存('+this.$list.find('li.check').length+')');
        },

        takePhoto: function() {
            var that=this;

            sl.confirm('请上传车辆的产权证、行驶证、身份证照片',function() {
                app.takePhoto(function(res) {
                    that._appendImage(res);
                });
            });


        },

        pickImage: function() {
            var that=this;
            sl.confirm('请上传车辆的产权证、行驶证、身份证照片',function() {
                app.pickImage(function(res) {
                    that._appendImage(res);
                });
            });
        }

    });
});
