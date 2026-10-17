//! im 领域仓储。

pub mod im_config;
pub mod im_message;
pub mod im_servant;
pub mod web_socket;

use crate::db::DbPool;

#[derive(Clone)]
pub struct Repositories {
    pub im_config: im_config::ImConfigRepository,
    pub im_message: im_message::ImMessageRepository,
    pub im_servant: im_servant::ImServantRepository,
    pub web_socket: web_socket::WebSocketRepository,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            im_config: im_config::ImConfigRepository::new(pool.clone()),
            im_message: im_message::ImMessageRepository::new(pool.clone()),
            im_servant: im_servant::ImServantRepository::new(pool.clone()),
            web_socket: web_socket::WebSocketRepository::new(pool.clone()),
        }
    }
}
