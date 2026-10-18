//! analytics 领域仓储。

pub mod panel;
pub mod statistics;

use crate::db::DbPool;

#[derive(Clone)]
pub struct Repositories {
    pub statistics: statistics::StatisticsRepository,
    pub panel: panel::PanelRepository,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            statistics: statistics::StatisticsRepository::new(pool.clone()),
            panel: panel::PanelRepository::new(pool.clone()),
        }
    }
}
