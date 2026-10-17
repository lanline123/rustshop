//! wholesale 领域服务。

pub mod wholesale_client;

use std::sync::Arc;

/// wholesale 领域服务集合（由组合根装配一次，api 层只持有 `Arc<Services>`）。
#[derive(Clone)]
pub struct Services {
    pub wholesale_client: Arc<wholesale_client::WholesaleClientService>,
}
