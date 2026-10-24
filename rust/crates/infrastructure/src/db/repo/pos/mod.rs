//! pos 领域仓储。

pub mod device;

use crate::db::DbPool;

#[derive(Clone)]
pub struct Repositories {
    pub device: device::PosDeviceRepository,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            device: device::PosDeviceRepository::new(pool),
        }
    }
}
