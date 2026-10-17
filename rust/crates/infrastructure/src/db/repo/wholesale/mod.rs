//! wholesale 领域仓储。

pub mod wholesale_client;

use crate::db::DbPool;

#[derive(Clone)]
pub struct Repositories {
    pub wholesale_client: wholesale_client::WholesaleClientRepository,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            wholesale_client: wholesale_client::WholesaleClientRepository::new(pool.clone()),
        }
    }
}
